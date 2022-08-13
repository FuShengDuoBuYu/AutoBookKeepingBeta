package Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Base64;

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
        //把bitmap100%高质量压缩 到 output对象里
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    //把base64转为bitmap用于展示
    public static Bitmap base642bitmap(String base64Image){
        byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
