package org.homework;

public class Dog {
    private int []m;

    public Dog() {
        this.m = new int[101];
    }

    public static void main(String[] args){
        Dog d = new Dog();
        for(int i = 0; i < d.m.length; i++){
            d.m[i] = i;
        }
        for(int i = 0; i < d.m.length; i++){
            System.out.println(d.m[i]);
        }
    }
}

