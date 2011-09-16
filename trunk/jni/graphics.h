/* Adapted from:                                                            */
/*   TEXAS INSTRUMENTS, INC.                                                */
/*   IMGLIB  DSP Image/Video Processing Library                             */
void sobelFilter(uint32_t *input, int width, int height, uint32_t *output, int negative) {
        int H, O, V, i;
        int i00, i01, i02;
        int i10,      i12;
        int i20, i21, i22;
        int w = width;
        int numpx = width * (height - 1);
        for (i = 0; i < numpx - 1; ++i) {
                i00 = input[i    ] & 0xff;
                i01 = input[i    +1] & 0xff;
                i02 = input[i    +2] & 0xff;

                i10 = input[i+  w] & 0xff;
                i12 = input[i+  w+2] & 0xff;

                i20 = input[i+2*w] & 0xff;
                i21 = input[i+2*w+1] & 0xff;
                i22 = input[i+2*w+2] & 0xff;

                H = -  i00 - 2 * i01 -  i02 + i20 + 2 * i21 + i22;
                V = -  i00  +     i02
                        - 2 * i10  + 2 * i12
                        -     i20  +     i22;
                O = abs(H) + abs(V);
                if (O > 255) { O = 255; }
                if (negative) { O = 255 - O; }
                output[i + 1] = 0xff000000 | ((int) O << 16 | (int) O << 8 | (int) O);
        }
}







void RGBtoHSV(double r, double g, double b, double *h, double *s, double *v) {
	double min, max, delta;

	min = fmin(fmin(r, g), b);
	max = fmax(fmax(r, g), b);
	*v = max; // v
	delta = max - min;
	if (max != 0)
		*s = delta / max; // s
	else {
		// r = g = b = 0  // s = 0, v is undefined
		*s = 0;
		*h = -1;
		return;
	}
	if (r == max)
		*h = (g - b) / delta; // between yellow &	magenta
	else if (g == max)
		*h = 2 + (b - r) / delta; // between cyan & yellow
	else
		*h = 4 + (r - g) / delta; // between magenta & cyan
	*h *= 60; // degrees
	if (*h < 0)
		*h += 360;
}
