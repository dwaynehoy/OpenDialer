package com.squizbit.opendialer.fragment;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.squizbit.opendialer.R;
import com.squizbit.opendialer.library.FabActionable;
import com.squizbit.opendialer.library.widget.AutoSizeTextView;
import com.squizbit.opendialer.library.widget.DialLayout;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * A fragment which allows the user to input phone numbers into an dialer input field
 */
public class DialerFragment extends Fragment implements FabActionable {

    //region View fields
    @InjectView(R.id.editTextNumber)
    AutoSizeTextView mEditTextNumber;

    @InjectView(R.id.imageViewDeleteButton)
    ImageView mImageViewDeleteButton;

    @InjectView(R.id.numPadKeyOne)
    DialLayout mNumPadKeyOne;

    @InjectView(R.id.numPadKeyTwo)
    DialLayout mNumPadKeyTwo;

    @InjectView(R.id.numPadKeyThree)
    DialLayout mNumPadKeyThree;

    @InjectView(R.id.numPadKeyFour)
    DialLayout mNumPadKeyFour;

    @InjectView(R.id.numPadKeyFive)
    DialLayout mNumPadKeyFive;

    @InjectView(R.id.numPadKeySix)
    DialLayout mNumPadKeySix;

    @InjectView(R.id.numPadKeySeven)
    DialLayout mNumPadKeySeven;

    @InjectView(R.id.numPadKeyEight)
    DialLayout mNumPadKeyEight;

    @InjectView(R.id.numPadKeyNine)
    DialLayout mNumPadKeyNine;

    @InjectView(R.id.numPadKeyZero)
    DialLayout mNumPadKeyZero;

    @InjectView(R.id.numPadKeyHash)
    DialLayout mNumPadKeyHash;

    @InjectView(R.id.numPadKeyStar)
    DialLayout mNumPadKeyStar;
    //endregion

    private AsYouTypeFormatter mFormatter;
    private String mNumberString = "";
    private ToneGenerator mToneGenerator;
    private OnKeyDialedListener mOnKeyDialedListener;
    private OnNumberChangedListener mOnNumberChangedListener;

    private DialLayout.OnDialKeyPressStatusListener mKeyPress = new DialLayout.OnDialKeyPressStatusListener() {
        @Override
        public void onDialKeyPressedStateChanged(View touchedView, boolean pressed, boolean longPress) {
            if (pressed) {
                if (longPress) {
                    mNumberString = mNumberString.substring(0, mNumberString.length() - 1);
                }
                String keyCharacter = getKeyCharacter(touchedView, longPress);
                if(mOnKeyDialedListener != null){
                    mOnKeyDialedListener.onKeyDialedListener(keyCharacter);
                }

                mNumberString += keyCharacter;
                mEditTextNumber.setText(formatNumber(mNumberString));

                if(mOnNumberChangedListener != null){
                    mOnNumberChangedListener.onNumberChangeListener(mNumberString);
                }

            }

            triggerToneChange(touchedView, pressed);
        }
    };

    /**
     * Creates a new instance of the dialer fragment
     * @return A dialer fragment instance
     */
    public static Fragment newInstance() {
        return new DialerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialer_view, container, false);
        ButterKnife.inject(this, view);

        mNumPadKeyZero.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyOne.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyTwo.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyThree.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyFour.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyFive.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeySix.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeySeven.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyEight.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyNine.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyStar.setDialKeyPressStatusListener(mKeyPress);
        mNumPadKeyHash.setDialKeyPressStatusListener(mKeyPress);

        mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(Locale.getDefault().getCountry());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 80);
    }

    @OnClick(R.id.imageViewDeleteButton)
    protected void OnDeleteButtonClick(View view) {
        if (mNumberString.length() > 0) {
            mNumberString = mNumberString.substring(0, mNumberString.length() - 1);
            mEditTextNumber.setText(formatNumber(mNumberString));

            if(mOnNumberChangedListener != null){
                mOnNumberChangedListener.onNumberChangeListener(mNumberString);
            }
        }
    }

    @OnLongClick(R.id.imageViewDeleteButton)
    protected boolean onDeleteButtonLongClick(View view) {
        if (mNumberString.length() > 0) {
            mNumberString = "";
            mEditTextNumber.setText(formatNumber(mNumberString));
            mFormatter.clear();

            if(mOnNumberChangedListener != null){
                mOnNumberChangedListener.onNumberChangeListener(mNumberString);
            }

            return true;
        }

        return false;
    }

    /**
     * Sets a callback to be invoked when a user dials a number.
     * @param onKeyDialedListener The callback to invoke when the user dials a number.
     */
    public void setOnKeyDialedListener(OnKeyDialedListener onKeyDialedListener) {
        mOnKeyDialedListener = onKeyDialedListener;
    }

    /**
     * Sets a callback which will be invoked when the number changes
     * @param onNumberChangedListener The callback to be invoked when the user changes the number
     */
    public void setOnNumberChangedListener(OnNumberChangedListener onNumberChangedListener) {
        mOnNumberChangedListener = onNumberChangedListener;
    }

    private String getKeyCharacter(View key, boolean longPress) {
        String character = null;

        switch (key.getId()) {
            case R.id.numPadKeyZero:
                character = longPress ? getString(R.string.keypad_0_letters) : getString(R.string.keypad_0);
                break;
            case R.id.numPadKeyOne:
                character = getString(R.string.keypad_1);
                break;
            case R.id.numPadKeyTwo:
                character = getString(R.string.keypad_2);
                break;
            case R.id.numPadKeyThree:
                character = getString(R.string.keypad_3);
                break;
            case R.id.numPadKeyFour:
                character = getString(R.string.keypad_4);
                break;
            case R.id.numPadKeyFive:
                character = getString(R.string.keypad_5);
                break;
            case R.id.numPadKeySix:
                character = getString(R.string.keypad_6);
                break;
            case R.id.numPadKeySeven:
                character = getString(R.string.keypad_7);
                break;
            case R.id.numPadKeyEight:
                character = getString(R.string.keypad_8);
                break;
            case R.id.numPadKeyNine:
                character = getString(R.string.keypad_9);
                break;
            case R.id.numPadKeyStar:
                character = getString(R.string.keypad_star);
                break;
            case R.id.numPadKeyHash:
                character = getString(R.string.keypad_hash);
                break;
        }

        return character;
    }

    private void triggerToneChange(View key, boolean pressed) {
        int tone = -1;

        switch (key.getId()) {
            case R.id.numPadKeyZero:
                tone = ToneGenerator.TONE_DTMF_0;
                break;
            case R.id.numPadKeyOne:
                tone = ToneGenerator.TONE_DTMF_1;
                break;
            case R.id.numPadKeyTwo:
                tone = ToneGenerator.TONE_DTMF_2;
                break;
            case R.id.numPadKeyThree:
                tone = ToneGenerator.TONE_DTMF_3;
                break;
            case R.id.numPadKeyFour:
                tone = ToneGenerator.TONE_DTMF_4;
                break;
            case R.id.numPadKeyFive:
                tone = ToneGenerator.TONE_DTMF_5;
                break;
            case R.id.numPadKeySix:
                tone = ToneGenerator.TONE_DTMF_6;
                break;
            case R.id.numPadKeySeven:
                tone = ToneGenerator.TONE_DTMF_7;
                break;
            case R.id.numPadKeyEight:
                tone = ToneGenerator.TONE_DTMF_8;
                break;
            case R.id.numPadKeyNine:
                tone = ToneGenerator.TONE_DTMF_9;
                break;
            case R.id.numPadKeyStar:
                tone = ToneGenerator.TONE_DTMF_S;
                break;
            case R.id.numPadKeyHash:
                tone = ToneGenerator.TONE_DTMF_P;
                break;
        }

        if (pressed) {
            mToneGenerator.startTone(tone, -1);
        } else {
            mToneGenerator.stopTone();
        }
    }

    /**
     * Generate the formatted number by ignoring all non-dialable chars and stick the cursor to the
     * nearest dialable char to the left. For instance, if the number is  (650) 123-45678 and '4' is
     * removed then the cursor should be behind '3' instead of '-'.
     */
    private String formatNumber(CharSequence s) {
        // The index of char to the leftward of the cursor.
        String formatted = null;
        mFormatter.clear();
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == mEditTextNumber.length() - 1) {
                hasCursor = true;
            }
        }
        if (lastNonSeparator != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor);
        }
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        return hasCursor ? mFormatter.inputDigitAndRememberPosition(lastNonSeparator)
                : mFormatter.inputDigit(lastNonSeparator);
    }

    @Override
    public boolean onFabAction() {
        return false;
    }

    /**
     * Sets a number to be displayed by the dialer
     * @param number The number to be displayed
     */
    public void setNumber(@NonNull String number){
        mNumberString= number;
        mEditTextNumber.setText(formatNumber(number));
    }

    @Override
    public void onPause() {
        mToneGenerator.stopTone();
        super.onPause();
    }

    @Override
    public void onStop() {
        mToneGenerator.release();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public String getNumber() {
        return mNumberString;
    }

    /**
     * A callback which is invoked when a user taps a dialer key
     */
    public interface OnKeyDialedListener{

        /**
         * Invoked when a key has been activated/dialed by the user.
         * @param key The key that was activated/dialed by the user.
         */
        void onKeyDialedListener(String key);
    }

    /**
     * A callback which is invoked when the input number changes
     */
    public interface OnNumberChangedListener{

        /**
         * Invoked when the dialed number changes
         * @param number The new number
         */
        void onNumberChangeListener(String number);
    }
}
