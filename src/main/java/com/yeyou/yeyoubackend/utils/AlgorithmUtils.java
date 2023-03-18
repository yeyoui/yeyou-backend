package com.yeyou.yeyoubackend.utils;


import java.util.List;
import java.util.Objects;

/**
 * 算法工具类
 */
public class AlgorithmUtils {
    /**
     * 编辑距离算法（用于计算最相似的两组标签）
     * 原理：https://blog.csdn.net/DBC_121/article/details/104198838
     * 学习后自己实现了一边
     * @param tagList1
     * @param tagList2
     * @return
     */

    public static int minDistance(List<String> tagList1,List<String> tagList2){
        //获取l1 l2 长度
        int ln1=tagList1.size(),ln2=tagList2.size();
        //有标签为空
        if(ln1*ln2==0) return ln1+ln2;
        //tag1前i个值和tag2前j个值的最小操作数
        int [][]dp=new int[ln1+1][ln2+1];
        //初始化默认值
        for (int i = 1; i <= ln1; i++) {
            dp[i][0]=i;
        }
        for (int i = 1; i <= ln2; i++) {
            dp[0][i]=i;
        }

        for (int i = 1; i <= ln1; i++) {
            for (int j = 1; j <= ln2; j++) {
                int down=dp[i-1][j]+1;
                int left=dp[i][j-1]+1;
                int left_down=dp[i-1][j-1];
                if(!tagList1.get(i-1).equals(tagList2.get(j-1))){//当前位置值相同，不进行操作，操作数不+1
                    left_down++;
                }
                dp[i][j]=Math.min(left_down,Math.min(left,down));
            }
        }
        return dp[ln1][ln2];
    }

    public static int minDistance2(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }
}
