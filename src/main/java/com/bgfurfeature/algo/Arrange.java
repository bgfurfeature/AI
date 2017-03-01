package com.bgfurfeature.algo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by C.J.YOU on 2017/3/1.
 */
public class Arrange<E> {

    public List<String> dataG = new ArrayList<String>();

    /**
     * 计算A(n,k)
     * @param data
     * @param target
     * @param k
     */
    public void arrangeSelect(List<String> data,List<String> target, int k) {
        List<String> copyData;
        List<String> copyTarget;
        if(target.size() == k) {
            String str = "";
            for(String i : target)
                str += i;
            dataG.add(str);
        }

        for(int i=0; i<data.size(); i++) {

            copyData = new ArrayList<String>(data);
            copyTarget = new ArrayList<String>(target);

            copyTarget.add(copyData.get(i));
            copyData.remove(i);

            arrangeSelect(copyData, copyTarget,k);
        }
    }

}
