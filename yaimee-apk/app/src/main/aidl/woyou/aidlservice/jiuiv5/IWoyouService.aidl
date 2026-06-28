// Sunmi InnerPrinter service interface (woyou.aidlservice.jiuiv5)
// ตัดมาจากไฟล์ทางการ เก็บลำดับเมธอด 1-18 ไว้ครบ (ถึง printBitmap)
// ลำดับต้องตรงกับ service จริงเป๊ะ ห้ามสลับ
package woyou.aidlservice.jiuiv5;

import woyou.aidlservice.jiuiv5.ICallback;
import android.graphics.Bitmap;

interface IWoyouService
{
    // 1
    boolean postPrintData(String packageName, in byte[] data, int offset, int length);
    // 2
    int getFirmwareStatus();
    // 3
    String getServiceVersion();
    // 4
    void printerInit(in ICallback callback);
    // 5
    void printerSelfChecking(in ICallback callback);
    // 6
    String getPrinterSerialNo();
    // 7
    String getPrinterVersion();
    // 8
    String getPrinterModal();
    // 9
    void getPrintedLength(in ICallback callback);
    // 10
    void lineWrap(int n, in ICallback callback);
    // 11
    void sendRAWData(in byte[] data, in ICallback callback);
    // 12
    void setAlignment(int alignment, in ICallback callback);
    // 13
    void setFontName(String typeface, in ICallback callback);
    // 14
    void setFontSize(float fontsize, in ICallback callback);
    // 15
    void printText(String text, in ICallback callback);
    // 16
    void printTextWithFont(String text, String typeface, float fontsize, in ICallback callback);
    // 17
    void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);
    // 18
    void printBitmap(in Bitmap bitmap, in ICallback callback);
}
