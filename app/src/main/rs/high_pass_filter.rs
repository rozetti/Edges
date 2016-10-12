#pragma version(1)
#pragma rs java_package_name(uk.co.wideopentech.edges)

void root(uint32_t const *v_in, uint32_t *v_out, int x, int y)
{
    uint32_t i = *v_in;
    uint32_t o;

    o = (((i & 0xff00) >> 8) > 0x80) ? 0xff0000ff : 0xffffffff;

    *v_out = o;
}