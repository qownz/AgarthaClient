#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform ColorEditConfig {
	vec4 Adjustments;
	// rgb = tint colour, a = strength. Declaration order must match the order the
	// uniforms are written in ColorSettingsController and listed in the .json.
	vec4 Tint;
};

out vec4 fragColor;

vec3 rgbToHsv(vec3 color) {
	vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
	vec4 p = mix(vec4(color.bg, K.wz), vec4(color.gb, K.xy), step(color.b, color.g));
	vec4 q = mix(vec4(p.xyw, color.r), vec4(color.r, p.yzx), step(p.x, color.r));
	float d = q.x - min(q.w, q.y);
	float e = 1.0e-10;
	return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsvToRgb(vec3 color) {
	vec3 rgb = clamp(abs(mod(color.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
	return color.z * mix(vec3(1.0), rgb, color.y);
}

void main() {
	vec4 sampled = texture(InSampler, texCoord);
	vec3 hsv = rgbToHsv(sampled.rgb);

	hsv.x = fract(hsv.x + Adjustments.x / 360.0);
	hsv.y = clamp(hsv.y * Adjustments.y, 0.0, 1.0);

	vec3 color = hsvToRgb(hsv) * Adjustments.z;
	color = (color - 0.5) * Adjustments.w + 0.5;

	// Global colour grade. Rebuilding the pixel from its luminance times a
	// luma-normalised tint pushes every hue toward the tint without darkening the
	// frame, so the world reads as themed rather than as a flat sheet of colour.
	if (Tint.a > 0.0) {
		const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);
		float luma = dot(color, LUMA);
		float tintLuma = max(dot(Tint.rgb, LUMA), 1.0e-4);
		vec3 colorized = luma * (Tint.rgb / tintLuma);
		color = mix(color, colorized, clamp(Tint.a, 0.0, 1.0));
	}

	fragColor = vec4(clamp(color, 0.0, 1.0), sampled.a);
}
