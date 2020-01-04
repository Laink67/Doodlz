package ru.laink.doodlz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Субкласс DialogFragment, отображаемый командой меню для выбора толщины линии
public class LineWidthDialogFragment extends MainDialogFragment {
    private ImageView widthImageView;

    // Создает и возвращает AlertDialog
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Cоздание диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View lineWidthDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_line_width, null);
        builder.setView(lineWidthDialogView);

        builder.setTitle(R.string.title_line_width_dialog);
        // Получение ImageView
        widthImageView = lineWidthDialogView.findViewById(R.id.widthImageView);

        //Настройка widthSeekBar
        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        final SeekBar widthSeekBar = lineWidthDialogView.findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(lineWidthChanged);
        widthSeekBar.setProgress(doodleView.getLineWidth());

        // Добавлени кноппки Set Line Width
        builder.setPositiveButton(R.string.button_set_line_width, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doodleView.setLineWidth(widthSeekBar.getProgress());
            }
        });

        return builder.create();
    }

    // OnSeekBarChangeListener для SeekBar в диалоговом окне толщины линии
    private final SeekBar.OnSeekBarChangeListener lineWidthChanged =
            new SeekBar.OnSeekBarChangeListener() {
                final Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap); // Рисеут на Bitmap

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Настройка объекта Paint для текущего значения SeekBar
                    Paint paint = new Paint();
                    paint.setColor(getDoodleFragment().getDoodleView().getDrawingColor());
                    paint.setStrokeWidth(progress);

                    //Стирание объекта Bitmap и перерисовка линии
                    bitmap.eraseColor(getResources().getColor(android.R.color.transparent, getContext().getTheme()));
                    canvas.drawLine(30, 50, 370, 50, paint);
                    widthImageView.setImageBitmap(bitmap);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };


}
