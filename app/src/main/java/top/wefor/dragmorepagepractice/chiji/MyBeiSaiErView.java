package top.wefor.dragmorepagepractice.chiji;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import top.wefor.dragmorepagepractice.R;

/**
 * edited by ice at 2017/11/2.
 *
 * Created by ${liumegnqiang} on 2017/5/19.
 * http://blog.csdn.net/lmq121210/article/details/72528674
 */

public class MyBeiSaiErView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;

    private boolean isStart = false;
    private boolean isSurfaceCreated = false;
    private boolean isThreadRun = false;
    private long mDelayMills;

    public MyBeiSaiErView(Context context) {
        super(context);
    }

    public MyBeiSaiErView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
    }

    /**
     * 确保调用此方法后动画才开始执行。
     */
    public void start(long delay) {
        isStart = true;
        mDelayMills = delay;
        if (isSurfaceCreated && !isThreadRun) {
            isThreadRun = true;
            new Thread(new MyThread(mDelayMills)).start();
        }
    }

    public MyBeiSaiErView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceCreated = true;
        if (isStart && !isThreadRun) {
            isThreadRun = true;
            new Thread(new MyThread(mDelayMills)).start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public class MyThread extends Thread {

        private long delay;

        public MyThread(long delayMills) {
            this.delay = delayMills;
        }

        @Override
        public void run() {
            super.run();
            /**
             * 修改波浪的宽度
             */
            int measuredWidth = getMeasuredWidth();
            /**
             * 修改波浪的高度
             */
            int measuredHeight = getMeasuredHeight();
            /**
             * 总共平移的间隔
             */
            int totalWidth = 0;
            int totalWidth2 = 0;
            try {
                if (delay > 0)
                    sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    sleep(INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Canvas canvas = surfaceHolder.lockCanvas();
                try {
                    canvas.drawColor(Color.WHITE);

                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.FILL);

                    Path path = new Path();

                    path.moveTo(-measuredWidth + totalWidth, measuredHeight / PERCENT);
                    path.quadTo(-measuredWidth * 3 / 4 + totalWidth, measuredHeight * 5 / 8,
                            -measuredWidth / PERCENT + totalWidth, measuredHeight / PERCENT);
                    path.quadTo(-measuredWidth / 4 + totalWidth, measuredHeight * 3 / 8,
                            0 + totalWidth, measuredHeight / PERCENT);
                    path.quadTo(measuredWidth / 4 + totalWidth, measuredHeight * 5 / 8,
                            measuredWidth / PERCENT + totalWidth, measuredHeight / PERCENT);
                    path.quadTo(measuredWidth * 3 / 4 + totalWidth, measuredHeight * 3 / 8,
                            measuredWidth + totalWidth, measuredHeight / PERCENT);

                    path.lineTo(measuredWidth + totalWidth, measuredHeight);
                    path.lineTo(-measuredWidth + totalWidth, measuredHeight);
                    path.close();

                    Paint paint2 = new Paint();
                    paint2.setColor(getResources().getColor(R.color.up_color));
                    paint2.setAntiAlias(true);
                    paint2.setStyle(Paint.Style.FILL);

                    Path path2 = new Path();

                    path2.moveTo(-measuredWidth + totalWidth2, measuredHeight / PERCENT);
                    path2.quadTo(-measuredWidth * 3 / 4 + totalWidth2, measuredHeight * 3 / 8,
                            -measuredWidth / PERCENT + totalWidth2, measuredHeight / PERCENT);
                    path2.quadTo(-measuredWidth / 4 + totalWidth2, measuredHeight * 5 / 8,
                            0 + totalWidth2, measuredHeight / PERCENT);
                    path2.quadTo(measuredWidth / 4 + totalWidth2, measuredHeight * 3 / 8,
                            measuredWidth / PERCENT + totalWidth2, measuredHeight / PERCENT);
                    path2.quadTo(measuredWidth * 3 / 4 + totalWidth2, measuredHeight * 5 / 8,
                            measuredWidth + totalWidth2, measuredHeight / PERCENT);

                    path2.lineTo(measuredWidth + totalWidth2, measuredHeight);
                    path2.lineTo(-measuredWidth + totalWidth2, measuredHeight);
                    path2.close();

                    canvas.drawPath(path2, paint2);
                    canvas.drawPath(path, paint);

                    totalWidth += INTERVAL * getMeasuredWidth() / CYCLE;
                    totalWidth2 += INTERVAL * getMeasuredWidth() / CYCLE2;

                    if (totalWidth > getMeasuredWidth()) {
                        totalWidth = 0;
                    }
                    if (totalWidth2 > getMeasuredWidth()) {
                        totalWidth2 = 0;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

        }
    }

    public static final int PERCENT = 2;
    public static final int INTERVAL = 12;
    public static final int CYCLE = 1600;
    public static final int CYCLE2 = 800;
}
