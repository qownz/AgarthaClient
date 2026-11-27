#version 330

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

// Written by MotionBlurRenderer.runVelocity in this order. vec4 only: Minecraft's std140
// writer pads a vec3 to 16 bytes, GLSL does not, so a vec3 here would shift what follows.
layout(std140) uniform MotionBlurConfig {
	mat4 InvModelView;
	mat4 InvProjection;
	mat4 PrevModelView;
	mat4 PrevProjection;
	vec4 CameraDelta; // xyz: camera movement since last frame, in blocks
	vec4 Params;      // x: strength, y: sample budget, z: mode (0 scene, 1 full, 2 camera)
};

in vec2 texCoord;

out vec4 fragColor;

const float MAX_STREAK = 0.4; // screen fraction; a violent flick never smears the whole view
const int MAX_SAMPLES = 256;

// Where the point at this uv and depth was on screen last frame.
vec2 previousUv(vec2 uv, float depth) {
	vec4 view = InvProjection * vec4(vec3(uv, depth) * 2.0 - 1.0, 1.0);
	vec3 relative = (InvModelView * vec4(view.xyz / view.w, 1.0)).xyz + CameraDelta.xyz;
	vec4 clip = PrevProjection * (PrevModelView * vec4(relative, 1.0));
	if (clip.w <= 1.0e-5) {
		return uv; // behind last frame's camera: no usable history
	}
	return clip.xy / clip.w * 0.5 + 0.5;
}

float dither(vec2 p) {
	return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

vec2 limit(vec2 v) {
	float len = length(v);
	return len > MAX_STREAK ? v * (MAX_STREAK / len) : v;
}

float depthAt(ivec2 texel, ivec2 last) {
	return texelFetch(MainDepthSampler, clamp(texel, ivec2(0), last), 0).r;
}

void main() {
	// Nearest depth of the pixel and its four neighbours: edges of near objects stay whole.
	ivec2 texel = ivec2(gl_FragCoord.xy);
	ivec2 last = textureSize(MainDepthSampler, 0) - 1;
	float depth = depthAt(texel, last);
	depth = min(depth, depthAt(texel + ivec2(1, 0), last));
	depth = min(depth, depthAt(texel + ivec2(-1, 0), last));
	depth = min(depth, depthAt(texel + ivec2(0, 1), last));
	depth = min(depth, depthAt(texel + ivec2(0, -1), last));

	// The motion of a point at the far plane is the camera's own turning. Split the pixel's motion
	// into that part and the rest (movement through the world).
	vec2 full = texCoord - previousUv(texCoord, depth);
	vec2 camera = texCoord - previousUv(texCoord, 1.0);
	float cameraSq = dot(camera, camera);
	vec2 cameraPart = cameraSq > 1.0e-12
			? camera * (clamp(dot(full, camera), 0.0, cameraSq) / cameraSq)
			: vec2(0.0);

	int mode = int(Params.z + 0.5);
	vec2 velocity = limit(mode == 0 ? full - cameraPart : (mode == 2 ? cameraPart : full));

	vec4 center = texture(MainSampler, texCoord);
	float speed = length(velocity);
	if (speed * Params.x < 1.0e-6) {
		fragColor = center; // standing still: nothing to blur
		return;
	}

	int budget = clamp(int(Params.y), 1, MAX_SAMPLES);
	int samples = clamp(int(ceil(speed * float(budget))), min(4, budget), budget);
	vec2 stepUv = velocity * Params.x / float(samples);
	float first = -0.5 * float(samples); // centred on the pixel
	vec3 sum = vec3(0.0);
	for (int i = 0; i < MAX_SAMPLES; i++) {
		if (i >= samples) {
			break;
		}
		float offset = first + float(i) + dither(gl_FragCoord.xy + vec2(float(i), float(i) * 1.4));
		vec3 c = texture(MainSampler, texCoord + stepUv * offset).rgb;
		sum += c * c; // squared is close to linear light, so bright streaks keep their brightness
	}
	fragColor = vec4(sqrt(sum / float(samples)), center.a);
}
