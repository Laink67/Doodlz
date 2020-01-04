package ru.laink.doodlz;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Управляет DoodleView и обработкой событий акселерометра
public class MainActivityFragment extends Fragment {
    private DoodleView doodleView;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean dialogOnScreen = false;

    // Используется для обнаружения встряхивания устройства
    private static final int ACCELERATION_THRESHOLD = 100000;

    // Используется для идентификации запровсов на использование внешнего храилища (сохранения)
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        // Получение ссылки на DoodleView
        doodleView = view.findViewById(R.id.doodleView);

        // Инициализация параметров ускорения
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening(); // Прослушивание события встряхивания
    }

    private void enableAccelerometerListening() {
        // Получение объекта SensorManager
        SensorManager sensorManager = (SensorManager) getActivity().
                getSystemService(Context.SENSOR_SERVICE);

        // Регистрация для прослушивания событий акселерометра
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening(); // Прекарщения прослушивания событий акселерометра
    }

    // Отказ от прослушиваний событий акселерометра
    private void disableAccelerometerListening() {
        // Получение объекта SensorManager
        SensorManager sensorManager = (SensorManager) getActivity().
                getSystemService(Context.SENSOR_SERVICE);

        // Прекращение прослушвания событий акселерометра
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

    }

    // Обработчик для событий акселерометра
    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                // Провервка встряхивания по показаниям акселерометра
                @Override
                public void onSensorChanged(SensorEvent event) {
                    // На экране не должно быть других диалоговых окон
                    if (!dialogOnScreen) {
                        // Получить значения для x, y, z для SensorEvent
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        // Сохраниить предыдущие данные ускорения
                        lastAcceleration = currentAcceleration;

                        // Вычислить текущее ускорение
                        currentAcceleration = x * x + y * y + z * z;

                        // Вычисдить изменение ускорения
                        acceleration = currentAcceleration * (currentAcceleration - lastAcceleration);

                        if (acceleration > ACCELERATION_THRESHOLD)
                            confirmErase();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

    // Подтверждение стирания рисунка
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doodle_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialogFragment = new ColorDialogFragment();
                colorDialogFragment.show(getFragmentManager(), "color dialog");
                return true;
            case R.id.delete_drawing:
                confirmErase();// Получить подтверждение перед стиранием
                return true;
            case R.id.line_width:
                LineWidthDialogFragment lineWidthDialogFragment = new LineWidthDialogFragment();
                lineWidthDialogFragment.show(getFragmentManager(), "line width dialog");
                return true;
            case R.id.save:
                saveImage();// Проверить разрешение и сохранить рисунок
                return true;
            case R.id.print:
                doodleView.printImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Запрашивает разрешение, если нужно, и сохраняет изображение
    private void saveImage() {
        // Проверка разрешения
        if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Окно для объяснения необходимости разрешения
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // Назначить сообщение AlertDialog
                builder.setMessage(R.string.permission_explanation);

                // Добавить ОК
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Запросить разрешение
                        requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                    }
                });

                // Отображение диалогового окна
                builder.create().show();
            } else {
                // Запросить разрешение
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_IMAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Сохранить изображение
            doodleView.saveImage();
        }
    }

    // Вызывается, когда пользователь предоставляет или отклоняет разрешение для созранения изображения
    private void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            doodleView.saveImage();
    }

    // Возвращает объект DoodleView
    public DoodleView getDoodleView() {
        return doodleView;
    }

    // Проверяет отображается ли диалоговое окно
    public void setDialogOnScreen(boolean visible){
        dialogOnScreen = visible;
    }
}
