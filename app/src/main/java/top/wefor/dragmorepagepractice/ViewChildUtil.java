package top.wefor.dragmorepagepractice;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created on 2018/1/4.
 *
 * @author ice
 */

public class ViewChildUtil {

    /**
     * 广度遍历 @checkView 里能滚动的子view，直到找到第scrollChildPosition+1个为止。
     */
    public static View findChildScrollView(@NonNull View checkView, int scrollChildPosition) {
        Queue<View> checkList = new ArrayDeque<>();
        checkList.offer(checkView);

        int indexOfScrollView = -1;

        while (!checkList.isEmpty()) {
            View view = checkList.poll();
            if (view.canScrollVertically(1) || view.canScrollVertically(-1)) {
                indexOfScrollView++;
                if (indexOfScrollView >= scrollChildPosition) {
                    return view;
                }
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    checkList.offer(viewGroup.getChildAt(i));
                }
            }
        }
        return null;
    }

}
