#version 330

uniform sampler2D Sample0Sampler;
uniform sampler2D Sample1Sampler;
uniform sampler2D Sample2Sampler;
uniform sampler2D Sample3Sampler;
uniform sampler2D Sample4Sampler;
uniform sampler2D Sample5Sampler;
uniform sampler2D Sample6Sampler;
uniform sampler2D Sample7Sampler;
uniform sampler2D Sample8Sampler;
uniform sampler2D Sample9Sampler;
uniform sampler2D Sample10Sampler;
uniform sampler2D Sample11Sampler;

// Written by MotionBlurRenderer.blendFrames: twelve frame weights, oldest first, then
// Info.x = 1 / total weight and Info.y = how many frames carry weight.
layout(std140) uniform FrameBlendConfig {
	vec4 WeightsA;
	vec4 WeightsB;
	vec4 WeightsC;
	vec4 Info;
};

in vec2 texCoord;

out vec4 fragColor;

vec3 linear(sampler2D frame) {
	vec3 c = texture(frame, texCoord).rgb;
	return c * c;
}

void main() {
	int count = int(Info.y + 0.5);
	vec3 sum = linear(Sample0Sampler) * WeightsA.x;
	if (count > 1) sum += linear(Sample1Sampler) * WeightsA.y;
	if (count > 2) sum += linear(Sample2Sampler) * WeightsA.z;
	if (count > 3) sum += linear(Sample3Sampler) * WeightsA.w;
	if (count > 4) sum += linear(Sample4Sampler) * WeightsB.x;
	if (count > 5) sum += linear(Sample5Sampler) * WeightsB.y;
	if (count > 6) sum += linear(Sample6Sampler) * WeightsB.z;
	if (count > 7) sum += linear(Sample7Sampler) * WeightsB.w;
	if (count > 8) sum += linear(Sample8Sampler) * WeightsC.x;
	if (count > 9) sum += linear(Sample9Sampler) * WeightsC.y;
	if (count > 10) sum += linear(Sample10Sampler) * WeightsC.z;
	if (count > 11) sum += linear(Sample11Sampler) * WeightsC.w;
	fragColor = vec4(sqrt(max(sum * Info.x, vec3(0.0))), 1.0);
}
