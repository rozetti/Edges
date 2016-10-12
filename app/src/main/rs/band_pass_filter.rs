#pragma version(1)
#pragma rs java_package_name(uk.co.wideopentech.edges)

void root(uint32_t const *v_in, uint32_t *v_out, int x, int y)
{
    uint32_t i = *v_in;
    uint32_t o;

    uint8_t b = (i & 0xff00) >> 8;
    o = (b > 0x70 && b < 0x90) ? 0xffff0000 : 0xffffffff;

    *v_out = o;
}
