package ru.laink.doodlz;

import android.content.Context;

import androidx.fragment.app.DialogFragment;

public class MainDialogFragment extends DialogFragment {
    //Получение ссылки на MainActivityFragment
    MainActivityFragment getDoodleFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.doodleFragment);
    }

    // Сообщает DoodleFragment, что диалоговое окно находится на экране
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);
    }

    // Сообщает MainActivityFragment, что диалогове окно не отображается

    @Override
    public void onDetach() {
        super.onDetach();

        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(false);
    }

}
