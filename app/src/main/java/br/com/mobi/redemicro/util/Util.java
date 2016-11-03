package br.com.mobi.redemicro.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static final String TAG = Util.class.getSimpleName();

    private final Context ctx;
    public static boolean smsSend = true;
    public static boolean smsDelivered = true;
    public static final Locale LOCAL = new Locale("pt", "BR");
    private int index = 0;

    public enum DrawableMode {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public Util(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Método estático (static) que converte uma string qualquer em um arquivo
     * hash de criptografia MD5, gerando 32 algarismos.<br /> Impossível reaver
     * o arquivo após sua criptografia.
     *
     * @param senha String de qualquer tamanho;
     * @return String Retorna um arquivo criptografado no formato MD5.
     * @throws NoSuchAlgorithmException Esta exceção é lançada quando um
     *                                  determinado algoritmo criptográfico é solicitadas, mas não está
     *                                  disponível no ambiente.
     */
    public String md5(String senha) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        BigInteger hash = new BigInteger(1, md.digest(senha.getBytes()));
        String hash2 = hash.toString(16);
        if (hash2.length() < 32) {
            int max = 32 - hash2.length();
            for (int i = 0; i < max; i++) {
                hash2 = "0" + hash2;
            }
        }
        return hash2;
    }

    /**
     * Função retorna distância em metros a partir de uma latitude e longitude
     * de inicio e uma latitude e longitude de destino.
     *
     * @param orglat  double Latitude de início
     * @param orglon  double Longitude de início
     * @param destlat double Latitude de destino
     * @param destlon double Longitude de destino
     * @return double distância em metros entre as posições geográficas.
     */
    public double haversine(double orglat, double orglon, double destlat, double destlon) {
        orglat = orglat * Math.PI / 180;
        orglon = orglon * Math.PI / 180;
        destlat = destlat * Math.PI / 180;
        destlon = destlon * Math.PI / 180;

        double raioterra = 6378140; // METROS
        double dlat = destlat - orglat;
        double dlon = destlon - orglon;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(orglat) * Math.cos(destlat) * Math.pow(Math.sin(dlon / 2), 2);
        double distancia = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return raioterra * distancia;
    }

    /**
     * Função obtem nível atual da bateria
     *
     * @return float
     * Percentual de 0 a 100% de bateria
     * Retornará -1 em caso de erro
     */
    public float batteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = this.ctx.registerReceiver(null, ifilter);
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            return (level / (float) scale) * 100;
        } else {
            return -1;
        }
    }

    public String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) this.ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this.ctx, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this.ctx, 0, new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        this.ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                Util.smsSend = getResultCode() == Activity.RESULT_OK;
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        this.ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Util.smsDelivered = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        Util.smsDelivered = false;
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();

        if (message.length() > 160) {
            ArrayList<String> parts = sms.divideMessage(message);

            ArrayList<PendingIntent> sentList = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveredList = new ArrayList<PendingIntent>();
            for (int i = 0; i < parts.size(); i++) {
                sentList.add(sentPI);
                deliveredList.add(deliveredPI);
            }

            sms.sendMultipartTextMessage(phoneNumber, null, parts, sentList, deliveredList);
        } else {
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }
    }

    /**
     * Função para adicionar zero a esquerda e manté-los no respectivo numeral.
     * <br /> O valor de zeros para adicionar não deve ser superior a String
     * completa, por exemplo. <br /><br />
     * <p>
     * valor = 123<br /> zeros = 2<br /><br />
     * <p>
     * Retornará exceção a quantidade de zeros deve ser igual ou maior ao
     * numeral informado.<br /><br />
     * <p>
     * Forma correta:<br /><br />
     * <p>
     * valor = 123<br /> zeros = 4<br /><br /> retorno 0123 (quatro algarimos)
     *
     * @param valor Object Valor que deseja adicionar zeros
     * @param zeros int quantidade de zeros para adicionar
     * @return String
     */
    public String zeroFill(Object valor, int zeros) {
        String sValor = String.valueOf(valor);

        if (sValor.length() > zeros) {
            return sValor;
        }

        int restantes = zeros - sValor.length();
        StringBuilder zadd = new StringBuilder();
        for (int i = 0; i < restantes; i++) {
            zadd.append("0");
        }

        return zadd.append(sValor).toString();
    }

    /**
     * Realiza a formatação do valor de acordo com a mascara enviada A másca só
     * funciona para os caracteres numericos, caso seja enviado letras ou
     * caracteres especiais esses serão removidos.
     *
     * @param valor   String Apenas números serão mascarados outros caracteres
     *                serão removidos
     * @param mascara String Exemplo: ###.###.###-##
     * @return String
     */
    public String mask(String valor, String mascara) {
        String dado = "";
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (Character.isDigit(c)) {
                dado += c;
            }
        }

        int indMascara = mascara.length();
        int indCampo = dado.length();

        for (; indCampo > 0 && indMascara > 0; ) {
            if (mascara.charAt(--indMascara) == '#') {
                indCampo--;
            }
        }

        String saida = "";
        for (; indMascara < mascara.length(); indMascara++) {
            saida += ((mascara.charAt(indMascara) == '#') ? dado.charAt(indCampo++) : mascara.charAt(indMascara));
        }
        return saida;
    }

    public String formatMoney(double d) {
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(LOCAL));
        return df.format(d);
    }

    public double moneyToDouble(String s) {
        if (!TextUtils.isEmpty(s)) {
            return Double.parseDouble(s.replace(",", "."));
        } else {
            return 0;
        }
    }

    public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Função redimensiona a imagem em um tamanho menor.
     */
    public Bitmap redimensionarImagem(Bitmap bitmap, int width, int height) throws IOException {
        try {
            int imgW = bitmap.getWidth();
            int imgH = bitmap.getHeight();
            if ((imgW <= width) || (imgH <= height)) {
                width = imgW;
                height = imgH;
            } else {
                double scale1 = Double.parseDouble(String.valueOf(width)) / Double.parseDouble(String.valueOf(imgW));
                double scale2 = Double.parseDouble(String.valueOf(height)) / Double.parseDouble(String.valueOf(imgH));
                double scale = (scale1 > scale2) ? scale2 : scale1;

                Long w = Math.round(imgW * scale);
                Long h = Math.round(imgH * scale);

                width = w.intValue();
                height = h.intValue();
            }

            Bitmap mBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            return mBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Não foi possível redimencionar o arquivo!");
            return bitmap;
        }
    }

    public byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            f.close();
        }
        return new byte[0];
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Função remove as máscaras da String; A função considerá máscara tudo que
     * não for número.
     *
     * @param s String
     * @return String
     */
    public String onlyNumber(String s) {
        StringBuilder unmask = new StringBuilder();
        if (s != null) {
            for (int i = 0; i < s.length(); i++) {
                if (Character.isDigit(s.charAt(i))) {
                    unmask.append(s.charAt(i));
                }
            }
        }
        return unmask.toString();
    }

    public int parseInt(String s) {
        if (!TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s)) {
            return Integer.parseInt(s);
        } else {
            return 0;
        }
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see Random#nextInt(int)
     */
    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public float convertDpToPixel(float dp) {
        Resources resources = ctx.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public float convertPixelsToDp(float px) {
        Resources resources = ctx.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    /**
     * Função para calcular data adicionando ou removendo dias.
     *
     * @param date Date
     * @param dias int O valor adotado pode ser negativo.
     * @return Date
     */
    public Date addDias(Date date, int dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, dias);
        return calendar.getTime();
    }

    public Bitmap convertFilePathToBitmap(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 150;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Função checa CPF válido.
     *
     * @param CPF String
     * @return boolean
     */
    public boolean isCPF(String CPF) {
        CPF = onlyNumber(CPF);
        if (CPF.equals("00000000000") || CPF.equals("11111111111")
                || CPF.equals("22222222222") || CPF.equals("33333333333")
                || CPF.equals("44444444444") || CPF.equals("55555555555")
                || CPF.equals("66666666666") || CPF.equals("77777777777")
                || CPF.equals("88888888888") || CPF.equals("99999999999")
                || (CPF.length() != 11)) {
            return false;
        }
        char dig10, dig11;
        int sm, i, r, num, peso;

        try {
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }
            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig10 = '0';
            } else {
                dig10 = (char) (r + 48);
            }
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig11 = '0';
            } else {
                dig11 = (char) (r + 48);
            }

            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10))) {
                return true;
            } else {
                return false;
            }
        } catch (InputMismatchException erro) {
            return false;
        }

    }

    public boolean isEmail(String s) {
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    public boolean isDate(String dateToValidate, String dateFromat) {
        if (dateToValidate == null) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
        sdf.setLenient(false);

        try {
            Date date = sdf.parse(dateToValidate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public double convertIntegerToDoubleRealCents(Integer integer) {
        if (integer == null) return 0;
        String v = String.valueOf(integer);
        if (v.length() <= 1) return 0;
        String cents = v.substring(v.length() - 2);
        String real = v.substring(0, v.length() - 2);

        return Double.parseDouble(real + "." + cents);
    }

    public RelativeLayout.LayoutParams buttonShowCase(int vertical, int horizontal) {
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(vertical);
        lps.addRule(horizontal);

        int margin = ((Number) (ctx.getResources().getDisplayMetrics().density * 50)).intValue();
        lps.setMargins(margin, margin, margin, margin);
        return lps;
    }

    /**
     * Função calcula a idade.
     *
     * @param dataNasc Date Data de Nascimento
     * @return int idada
     */
    public int calculaIdade(Date dataNasc) {
        Calendar dateOfBirth = new GregorianCalendar();
        dateOfBirth.setTime(dataNasc);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

        dateOfBirth.add(Calendar.YEAR, age);
        if (today.before(dateOfBirth)) {
            age--;
        }
        return age;
    }

    public String mesPorExtenso(int mes) {
        String[] meses = new String[]{"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return meses[mes];
    }

    public String mesPorExtensoCurto(int mes) {
        String[] meses = new String[]{"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        return meses[mes];
    }

    public String diaSemanaPorExtenso(int dia) {
        String[] dias = new String[]{"Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"};
        return dias[dia];
    }

    public String diaSemanaPorExtensoCurto(int dia) {
        String[] dias = new String[]{"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        return dias[dia];
    }

    public String stripTags(String text) {
        if (text != null) {
            return text.replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", "").trim();
        } else {
            return "";
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public Bitmap decodeUrlToBitmap(String url) {
        try {
            return BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (Exception e) {
            return null;
        }
    }

    public Long forceDateToLong(Date data) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("GMT-3"));
            String ds = format.format(data.getTime());
            return format.parse(ds).getTime();
        } catch (ParseException e) {
            return 0l;
        }
    }

    public String smartCut(String s, int len) {
        if (s != null) {
            if (s.length() > len) {
                return s.substring(0, len);
            } else {
                return s;
            }
        }
        return "";
    }


    public ArrayAdapter<String> createAdapter(Context context, String[] arrays) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, arrays);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerArrayAdapter;
    }

    public String unaccent(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
