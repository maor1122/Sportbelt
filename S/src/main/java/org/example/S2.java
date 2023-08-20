package org.example;

import java.util.Scanner;


public class S2 {
    public static void main(String[] args) {
        int[] arr = getInput();
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            int curr = arr[i];
            if (curr == 2 || curr <= 0) {
                System.out.println(-1);
                continue;
            }
            if(curr == 1){
                System.out.println(1);
                continue;
            }
            int matrixSize = curr*curr;
            int counter = 1, half = (int)((double)matrixSize/2+0.5);
            for (int j = 0; j*2 < matrixSize; j++) {
                System.out.print((counter)+" ");
                if((j*2+1)%curr==0) System.out.println();
                if(j*2+1 < matrixSize)
                    System.out.print((half+counter++)+" ");
                if((j*2+2)%curr==0) System.out.println();
            }
        }
    }

    private static int[] getInput(){
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int[] output = new int[n];
        for(int i=0;i<n;i++) {
            output[i] = scanner.nextInt();
        }
        return output;
    }
}