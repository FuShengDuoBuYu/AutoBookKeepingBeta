package Util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    //将bitmap转为圆形
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas( output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias( true);
        paint.setFilterBitmap( true);
        paint.setDither( true);
        canvas.drawARGB( 0, 0, 0, 0);
        paint.setColor( color);
        //在画布上绘制一个圆
        canvas.drawCircle( bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap( bitmap, rect, rect, paint);
        return output;
    }

    //将bitmap转为base64便于存储
    public static String bitmap2Base64(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }

    public static Bitmap scaleDown(Bitmap bitmap, int maxSize){
        if(bitmap == null || maxSize <= 0){
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longestSide = Math.max(width, height);
        if(longestSide <= maxSize){
            return bitmap;
        }
        float ratio = maxSize / (float) longestSide;
        return Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(width * ratio)), Math.max(1, Math.round(height * ratio)), true);
    }

    //把base64转为bitmap用于展示
    public static Bitmap base642bitmap(String base64Image){
        if(base64Image == null || base64Image.trim().isEmpty()){
            return null;
        }
        try {
            byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            int sampleSize = 1;
            while(bounds.outWidth / sampleSize > 512 || bounds.outHeight / sampleSize > 512){
                sampleSize *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    //把drawable转为bitmap
    public static Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w,h,config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    //获取屏幕截图并高斯模糊
    public static Bitmap getWindowScreenShot(Activity activity){
        //去除状态栏
//        View view = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        //压缩
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        //高斯模糊
        RenderScript rs = RenderScript.create(activity);
        final Allocation input = Allocation.createFromBitmap(rs, bitmap);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(25);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
        return bitmap;
    }

}
