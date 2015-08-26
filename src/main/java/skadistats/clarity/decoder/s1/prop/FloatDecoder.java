package skadistats.clarity.decoder.s1.prop;

import skadistats.clarity.decoder.BitStream;
import skadistats.clarity.model.s1.PropFlag;
import skadistats.clarity.model.s1.SendProp;

public class FloatDecoder implements PropDecoder<Float> {

    private static final int COORD_INTEGER_BITS = 14;
    private static final int COORD_FRACTIONAL_BITS = 5;
    private static final int COORD_DENOMINATOR = (1 << COORD_FRACTIONAL_BITS);
    private static final float COORD_RESOLUTION = (1.0f / COORD_DENOMINATOR);

    private static final int COORD_INTEGER_BITS_MP = 11;
    private static final int COORD_FRACTIONAL_BITS_MP_LOWPRECISION = 3;
    private static final int COORD_DENOMINATOR_LOWPRECISION = (1 << COORD_FRACTIONAL_BITS_MP_LOWPRECISION);
    private static final float COORD_RESOLUTION_LOWPRECISION = (1.0f / COORD_DENOMINATOR_LOWPRECISION);

    @Override
    public Float decode(BitStream stream, SendProp prop) {
        int flags = prop.getFlags();
        if ((flags & PropFlag.COORD) != 0) {
            return decodeCoord(stream);
        } else if ((flags & PropFlag.COORD_MP) != 0) {
            return decodeFloatCoordMp(stream, false, false);
        } else if ((flags & PropFlag.COORD_MP_LOW_PRECISION) != 0) {
            return decodeFloatCoordMp(stream, false, true);
        } else if ((flags & PropFlag.COORD_MP_INTEGRAL) != 0) {
            return decodeFloatCoordMp(stream, true, false);
        } else if ((flags & PropFlag.NO_SCALE) != 0) {
            return decodeNoScale(stream);
        } else if ((flags & PropFlag.NORMAL) != 0) {
            return decodeNormal(stream);
        } else if ((flags & PropFlag.CELL_COORD) != 0) {
            return decodeCellCoord(stream, prop.getNumBits());
        } else if ((flags & PropFlag.CELL_COORD_INTEGRAL) != 0) {
            return decodeCellCoordIntegral(stream, prop.getNumBits());
        } else {
            return decodeDefault(stream, prop.getNumBits(), prop.getHighValue(), prop.getLowValue());
        }
    }

    public float decodeCoord(BitStream stream) {
        return stream.readBitCoord();
    }

    public float decodeFloatCoordMp(BitStream stream, boolean integral, boolean lowPrecision) {
        int i = 0;
        int f = 0;
        boolean sign = false;
        float value = 0.0f;

        boolean inBounds = stream.readBitFlag();
        if (integral) {
            i = stream.readUBitInt(1);
            if (i != 0) {
                sign = stream.readBitFlag();
                value = stream.readUBitInt(inBounds ? COORD_INTEGER_BITS_MP : COORD_INTEGER_BITS) + 1;
            }
        } else {
            i = stream.readUBitInt(1);
            sign = stream.readBitFlag();
            if (i != 0) {
                i = stream.readUBitInt(inBounds ? COORD_INTEGER_BITS_MP : COORD_INTEGER_BITS) + 1;
            }
            f = stream.readUBitInt(lowPrecision ? COORD_FRACTIONAL_BITS_MP_LOWPRECISION : COORD_FRACTIONAL_BITS);
            value = i + ((float) f * (lowPrecision ? COORD_RESOLUTION_LOWPRECISION : COORD_RESOLUTION));
        }
        return sign ? -value : value;
    }

    public float decodeNoScale(BitStream stream) {
        return Float.intBitsToFloat(stream.readUBitInt(32));
    }

    public float decodeNormal(BitStream stream) {
        return stream.readBitNormal();
    }

    public float decodeCellCoord(BitStream stream, int numBits) {
        int v = stream.readUBitInt(numBits);
        return v + COORD_RESOLUTION * stream.readUBitInt(COORD_FRACTIONAL_BITS);
    }

    public float decodeCellCoordIntegral(BitStream stream, int numBits) {
        int v = stream.readUBitInt(numBits);
        return (float) v;
    }

    public float decodeDefault(BitStream stream, int numBits, float high, float low) {
        int t = stream.readUBitInt(numBits);
        float f = (float) t / ((1 << numBits) - 1);
        return f * (high - low) + low;
    }

}