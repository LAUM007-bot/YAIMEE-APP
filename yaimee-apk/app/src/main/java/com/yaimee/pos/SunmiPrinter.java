package com.yaimee.pos;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.IBinder;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

/**
 * เชื่อมต่อเครื่องปริ้นในตัวของ Sunmi (InnerPrinter) ผ่าน AIDL
 * วิธีพิมพ์: render ใบเสร็จเป็นรูปภาพ (รองรับภาษาไทย 100%) แล้วส่ง printBitmap
 */
public class SunmiPrinter {

    // ความกว้างจุดของกระดาษ: 80mm = 576, 58mm = 384
    private static final int PAPER_WIDTH = 576;

    private final Context ctx;
    private IWoyouService service;

    public SunmiPrinter(Context c) {
        ctx = c.getApplicationContext();
    }

    private final ServiceConnection conn = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IWoyouService.Stub.asInterface(binder);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

    public void bind() {
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        try {
            ctx.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } catch (Exception ignored) {}
    }

    public void unbind() {
        try { ctx.unbindService(conn); } catch (Exception ignored) {}
    }

    public boolean isReady() {
        return service != null;
    }

    private static final ICallback NOOP = new ICallback.Stub() {
        @Override public void onRunResult(boolean isSuccess) {}
        @Override public void onReturnString(String result) {}
        @Override public void onRaiseException(int code, String msg) {}
        @Override public void onPrintResult(int code, String msg) {}
    };

    /** รับ JSON ของบรรทัดใบเสร็จจาก JS แล้วพิมพ์ */
    public void printLines(final String linesJson) {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (service == null) return;
                    Bitmap bmp = render(linesJson, PAPER_WIDTH);
                    service.printerInit(NOOP);
                    service.setAlignment(0, NOOP);
                    service.printBitmap(bmp, NOOP);
                    service.lineWrap(3, NOOP);
                    // สั่งตัดกระดาษ (เครื่องที่มีตัวตัดเท่านั้น ถ้าไม่มีจะข้ามไป)
                    try { service.sendRAWData(new byte[]{0x1D, 0x56, 0x42, 0x00}, NOOP); } catch (Exception ignored) {}
                } catch (Exception ignored) {}
            }
        }).start();
    }

    /* ---------- render ใบเสร็จเป็น Bitmap ---------- */
    private static Bitmap render(String linesJson, int width) throws Exception {
        JSONArray arr = new JSONArray(linesJson);
        final int pad = 12;
        final int contentW = width - pad * 2;

        List<Object> draws = new ArrayList<>();   // StaticLayout หรือ "LINE"
        int totalH = pad;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String s = o.optString("s", "md");
            if ("line".equals(s)) {
                draws.add("LINE");
                totalH += 24;
                continue;
            }
            String t = o.optString("t", "");
            TextPaint p = paintFor(s);
            Layout.Alignment al = alignFor(o.optString("a", "left"));
            StaticLayout sl = new StaticLayout(t, p, contentW, al, 1.0f, 3f, false);
            draws.add(sl);
            totalH += sl.getHeight() + 4;
        }
        totalH += pad;

        Bitmap bmp = Bitmap.createBitmap(width, Math.max(totalH, 1), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setPathEffect(new DashPathEffect(new float[]{7, 5}, 0));

        int y = pad;
        for (Object d : draws) {
            if (d instanceof String) {
                int ly = y + 11;
                c.drawLine(pad, ly, width - pad, ly, linePaint);
                y += 24;
            } else {
                StaticLayout sl = (StaticLayout) d;
                c.save();
                c.translate(pad, y);
                sl.draw(c);
                c.restore();
                y += sl.getHeight() + 4;
            }
        }
        return bmp;
    }

    private static TextPaint paintFor(String s) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        if ("xl".equals(s)) { p.setTextSize(46f); p.setTypeface(Typeface.DEFAULT_BOLD); }
        else if ("lg".equals(s)) { p.setTextSize(38f); p.setTypeface(Typeface.DEFAULT_BOLD); }
        else if ("md".equals(s)) { p.setTextSize(32f); p.setTypeface(Typeface.DEFAULT); }
        else { p.setTextSize(27f); p.setTypeface(Typeface.DEFAULT); }
        return p;
    }

    private static Layout.Alignment alignFor(String a) {
        if ("center".equals(a)) return Layout.Alignment.ALIGN_CENTER;
        if ("right".equals(a)) return Layout.Alignment.ALIGN_OPPOSITE;
        return Layout.Alignment.ALIGN_NORMAL;
    }
}
