package ru.laink.doodlz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// Субкласс DialogFragment, отображаемый командой меню для выбора цвета
public class ColorDialogFragment extends MainDialogFragment {
    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private View colorView;
    private int color;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Cоздание диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View colorDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_color, null);

        builder.setView(colorDialogView);
        builder.setTitle(R.string.title_color_dialog);

        // Получение значений Seekbar и назначение слушателей onChange
        alphaSeekBar = colorDialogView.findViewById(R.id.alphaSeekBar);
        redSeekBar = colorDialogView.findViewById(R.id.redSeekBar);
        greenSeekBar = colorDialogView.findViewById(R.id.greenSeekBar);
        blueSeekBar = colorDialogView.findViewById(R.id.blueSeekBar);
        colorView = colorDialogView.findViewById(R.id.colorView);

        // Регистрация слушателей событий Seekbar
        alphaSeekBar.setOnSeekBarChangeListener(colorChangedListener);
        redSeekBar.setOnSeekBarChangeListener(colorChangedListener);
        greenSeekBar.setOnSeekBarChangeListener(colorChangedListener);
        blueSeekBar.setOnSeekBarChangeListener(colorChangedListener);

        // Использование текущего цвета линии для инициализации
        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        color = doodleView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        // Добавление кнопки назначения цвета
        builder.setPositiveButton(R.string.button_set_color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doodleView.setDrawingColor(color);
            }
        });

        return builder.create(); // Возвращение диалогового окна
    }

    //OnSeekBarChangeListener для компонентов SeekBar в диалоговом окне
    private final SeekBar.OnSeekBarChangeListener colorChangedListener = new SeekBar.OnSeekBarChangeListener() {
        //Отображени обновленного цвета
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) // Измененое пользователем (не программой)
                color = Color.argb(alphaSeekBar.getProgress(), redSeekBar.getProgress()
                        , greenSeekBar.getProgress(), blueSeekBar.getProgress());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}
