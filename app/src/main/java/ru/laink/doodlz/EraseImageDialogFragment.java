package ru.laink.doodlz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// Субкласс DialogFragment, отображаемый командой меню для стирания или встряхиванием устройства для стирания текущего рисунка
public class EraseImageDialogFragment extends MainDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Cоздание диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Назначение сообщения AlertDialog
        builder.setMessage(R.string.message_erase);

        // Добавление кнопки назначения цвета
        builder.setPositiveButton(R.string.button_set_color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDoodleFragment().getDoodleView().clear(); // Очистка
            }
        });

        // Добавление кнопки стирания
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }
}
