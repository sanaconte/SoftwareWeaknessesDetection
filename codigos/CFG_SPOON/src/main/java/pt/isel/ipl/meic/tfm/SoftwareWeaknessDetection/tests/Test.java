package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.tests;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) {
        int[] A = {1, 3, 6, 3, 1, 2};
        //int[] A = {1, 2, 2, 1};
        //int[] A = {7, 7, 7};
        //int[] A = {1, 2, 2, 3};
        System.out.println("Res: "+mult(A));
    }

    /**
     * hat, given an array A of N integers,
     * returns the smallest positive integer
     * (greater than 0) that does not occur in A.
     * @param A
     * @return
     */
    public static int solution(int[] A) {
        // write your code in Java SE 11
        List<Integer> collect =
                        Arrays.
                        stream(A)
                        .boxed()
                        .sorted(Collections.reverseOrder())
                        .collect(Collectors.toList());

        int greater = collect.get(0);
        int smallest = collect.get(1);
        int smallestPlusOne = smallest +1;
        int greaterPlusOne = greater + 1;

        if(greater < 0){
            return 1;
        }
        else if (smallestPlusOne  < greater){
            return smallestPlusOne;
        } else{
            return greaterPlusOne;
        }

    }


    public static boolean mult(int[] A) {
        // write your code in Java SE 11

        if(A.length %2 == 1){
            return false;
        }

        Map<Integer, List<Integer>> collect =
                Arrays.stream(A).boxed()
                        .collect(Collectors.groupingBy(el -> el));

        System.out.println(collect);

        return
                collect
                        .values()
                        .stream()
                        .allMatch(list -> list.size() % 2==0);

        /**
        boolean res = true;
        for(Map.Entry<Integer, List<Integer>> entry : collect.entrySet()){
            if(entry.getValue().size() %2 == 1){
                res = false;
            }
        }
        return res;
        */

    }
}
