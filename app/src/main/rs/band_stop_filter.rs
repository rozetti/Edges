#pragma version(1)
#pragma rs java_package_name(uk.co.wideopentech.edges)

void root(uint32_t const *v_in, uint32_t *v_out, int x, int y)
{
    uint32_t i = *v_in;
    uint32_t o;

    uint8_t b = (i & 0xff00) >> 8;
    if (b > 0x90)
    {
        o = 0xff0000ff;
    }
    else if (b < 0x70)
    {
        o = 0xff00ff00;
    }
    else
    {
        o = 0xff202020;
    }

    *v_out = o;
}
