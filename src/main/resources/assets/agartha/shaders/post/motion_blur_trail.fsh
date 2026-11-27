#version 330

uniform sampler2D MainSampler;
uniform sampler2D PrevSampler;

// Written by MotionBlurRenderer.trail. Params.x: how much of the past remains,
// Params.y: 0 keeps the brighter of now and the fading past, 1 mixes them evenly.
layout(std140) uniform TrailConfig {
	vec4 Params;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec4 now = texture(MainSampler, texCoord);
	vec3 past = texture(PrevSampler, texCoord).rgb;
	vec3 color = Params.y < 0.5
			? max(now.rgb, past * Params.x)
			: sqrt(mix(now.rgb * now.rgb, past * past, Params.x));
	fragColor = vec4(color, now.a);
}
