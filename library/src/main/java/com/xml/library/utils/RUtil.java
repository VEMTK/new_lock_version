package com.xml.library.utils;

import android.util.Log;

import java.util.Random;

/**
 * Created by xlc on 2016/12/30.
 */
public class RUtil {

    public static int get_proportion(double[] weightArrays) {

        int weightValue = getWeightRandom(weightArrays);

        if (weightValue >= 0) {

            return weightValue;
        }
        return -1;
    }

    private static double weightArraySum(double[] weightArrays) {
        double weightSum = 0;
        if (weightArrays != null) {
            for (double weightValue : weightArrays) {
                weightSum += weightValue;
            }
            return weightSum;
        }
        return weightSum;
    }

    private static int getWeightRandom(double[] weightArrays) {

        double weightSum = weightArraySum(weightArrays);

        double r1 = weightArrays[0] / weightSum;

        double r2 = weightArrays[1] / weightSum;

        double r3 = weightArrays[2] / weightSum;

        double radnum = Math.random();

        if (radnum >= 0 && radnum <= r1) {

            return 0;

        } else if (radnum > r1 && radnum <= (r2 + r1)) {

            return 1;

        } else if (radnum > (r1 + r2) && radnum <= (r1 + r2 + r3)) {

            return 2;
        }
        return -1;
    }

    public static int get_proportion(int[] weightArrays) {

        int tatol = getTalo(weightArrays);

        int radnum = new Random().nextInt(tatol);

        int l=weightArrays[0];

        int m=weightArrays[0]+weightArrays[1];

        if (radnum < l) {

            return 0;

        } else if (radnum >= l && radnum< m) {

            return 1;

        } else {

            return 2;
        }
    }
    private static int getTalo(int[] weightArrays) {
        int t = 0;

        for (int i = 0; i < weightArrays.length; i++) {

            t = t + weightArrays[i];
        }
        return t;
    }
}
