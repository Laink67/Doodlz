package ru.laink.doodlz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.print.PrintHelper;

import java.util.HashMap;
import java.util.Map;

// Предоставляет функции рисования, сохранения и печати
public class DoodleView extends View {
    // Смещение необходимое для продолжения рисования
    private static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap; // Область рисования для вывода или сохранения
    private Canvas bitmapCanvas; // Используется для рисования на bitmap
    private final Paint paintScreen; // Используется для вывода bitmap на экран
    private final Paint paintLine; // Используется для рисования линий на Bitmap

    // Данные нарисованных контуров Path и содержащихся в них точек
    private final Map<Integer, Path> pathMap = new HashMap<>();
    private final Map<Integer, Point> previousPointMap = new HashMap<>();

    public DoodleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paintScreen = new Paint();

        // Исходные параметры рисуемых линий
        paintLine = new Paint();
        paintLine.setAntiAlias(true); // Сглаживание краёв
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE); // Сплошная линия
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND); // Закруглённые концы
    }

    // Создание объектов Bitmap и Canvas на основании размеров View
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE); // Стриается белым цветом
    }

    // Очистка рисунка
    public void clear() {
        pathMap.clear(); //  Удалить все контуры
        previousPointMap.clear(); // Удалить все предыдущие точки
        bitmap.eraseColor(Color.WHITE); // Очитска изображения
        invalidate();// Перерисовать изображение
    }

    // Назначение цвета рисуемой линиии
    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    // Получение цвета рисуемой линии
    public int getDrawingColor() {
        return paintLine.getColor();
    }

    // Назначение толщины рисуемой линии
    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    // Получени толщины рисуемой линии
    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    // Перерисовка при обновлении DoodleView на экране
    @Override
    protected void onDraw(Canvas canvas) {
        // Перерисовка фона
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        // Для каждой выводимой линии
        for (Integer key : pathMap.keySet())
            canvas.drawPath(pathMap.get(key), paintLine); // Рисование линии

        super.onDraw(canvas);
    }

    // Обрабока событий касания
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // Тип события
        int actionIndex = event.getActionIndex(); // Указатель

        // Начало касания, конец или перемещение?
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate();// Перерисовка

        return true;
    }

    // Вызывается при касании
    private void touchStarted(float x, float y, int lineId) {
        Path path; // Для храения контура с заданным идентификатором
        Point point; // Для хранения последней точки в контуре

        // Если для lineId уже существует объект Path
        if (pathMap.containsKey(lineId)) {
            path = pathMap.get(lineId); // Получение Path
            path.reset(); // Очистка Path с началом нового касания
            point = previousPointMap.get(lineId); // Последняя точка Path
        } else {
            path = new Path();
            pathMap.put(lineId, path); // Добавление Path в Map
            point = new Point();
            previousPointMap.put(lineId, point); // Добавление point в Map
        }

        // Перход к координатам касания
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    // Перемещение пальца по экрану
    private void touchMoved(MotionEvent motionEvent) {

        // Для каждого указателя
        for (int i = 0; i < motionEvent.getPointerCount(); i++) {

            int pointerID = motionEvent.getPointerId(i); // Идентификатор указателя
            int pointerIndex = motionEvent.findPointerIndex(pointerID); // Индекс указателя

            //Если существует объект Path связанный с указателем
            if (pathMap.containsKey(pointerID)) {
                float newX = motionEvent.getX(pointerIndex);
                float newY = motionEvent.getY(pointerIndex);

                // Получить объект Path и предыдущий объект Point связанный с указателем
                Path path = pathMap.get(pointerID);
                Point point = previousPointMap.get(pointerID);

                // Величины смещения от последнего обновления
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                // Если расстояние достаточно велико
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    // Расширение контура до нововой точки
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    // Сохранение новых координат
                    point.x = (int) newX;
                    point.y = (int) newY;
                }

            }
        }
    }

    // При завершении касания
    private void touchEnded(int lineID) {
        Path path = pathMap.get(lineID);
        bitmapCanvas.drawPath(path, paintLine); // Прорисовка линии
        path.reset(); // Сброс объекта path
    }

    // Сохранение изображения в галерее
    public void saveImage() {
        // Имя состоит из префикса Doodlz и текущего времени
        final String name = "Doodlz" + System.currentTimeMillis() + ".jpg";

        // Сохранеине изображения в галлереи устройства
        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), bitmap, name, "Doodlz drawing"
        );

        // Если путь найден
        if (location != null) {
            // Вывод сообщени об успешном сохранении
            Toast message = Toast.makeText(getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        } else {
            // Вывод сообщени об ошибке
            Toast message = Toast.makeText(getContext(), R.string.message_error_saving, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        }
    }

    // Печать текущего рисунка
    public void printImage() {
        if (PrintHelper.systemSupportsPrint()) {

            PrintHelper printHelper = new PrintHelper(getContext());

            //Изображение масштабируется и выводится на печать
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("Doodlz Image", bitmap);
        } else {
            // Вывод сообщени об ошибке
            Toast message = Toast.makeText(getContext(), R.string.message_error_printing, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        }
    }

}
