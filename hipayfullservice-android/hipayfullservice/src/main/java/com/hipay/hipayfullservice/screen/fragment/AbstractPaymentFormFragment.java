package com.hipay.hipayfullservice.screen.fragment;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hipay.hipayfullservice.R;
import com.hipay.hipayfullservice.core.client.GatewayClient;
import com.hipay.hipayfullservice.core.models.PaymentProduct;
import com.hipay.hipayfullservice.core.models.Transaction;
import com.hipay.hipayfullservice.core.requests.order.PaymentPageRequest;
import com.hipay.hipayfullservice.screen.activity.ForwardWebViewActivity;
import com.hipay.hipayfullservice.screen.helper.FormHelper;
import com.hipay.hipayfullservice.screen.model.CustomTheme;

/**
 * Created by nfillion on 29/02/16.
 */

public abstract class AbstractPaymentFormFragment extends Fragment {

    private ProgressBar mProgressBar;
    OnCallbackOrderListener mCallback;
    protected LinearLayout mCardInfoLayout;

    protected abstract void launchRequest();
    public interface OnCallbackOrderListener {

        void onCallbackOrderReceived(Transaction transaction, Exception exception);
    }

    private EditText mCardOwner;
    private EditText mCardNumber;
    private EditText mCardExpiration;
    private EditText mCardCVV;
    private Button mPayButton;

    protected GatewayClient mGatewayClient;

    public static AbstractPaymentFormFragment newInstance(Bundle paymentPageRequestBundle, PaymentProduct paymentProduct, Bundle customTheme) {

        AbstractPaymentFormFragment fragment;

        Boolean isTokenizable = paymentProduct.isTokenizable();
        if (isTokenizable != null && isTokenizable == true) {

            fragment = new TokenizableCardPaymentFormFragment();

        } else {

            fragment = new UnsupportedPaymentFormFragment();
        }

        Bundle args = new Bundle();
        args.putBundle(PaymentPageRequest.TAG, paymentPageRequestBundle);
        args.putBundle(PaymentProduct.TAG, paymentProduct.toBundle());
        args.putBundle(CustomTheme.TAG, customTheme);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnCallbackOrderListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
            + " must implement OnCallbackOrderListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (savedInstanceState != null) {
        //    final int savedAvatarIndex = savedInstanceState.getInt(KEY_SELECTED_AVATAR_INDEX);
        //    if (savedAvatarIndex != GridView.INVALID_POSITION) {
        //        mSelectedAvatar = Avatar.values()[savedAvatarIndex];
        //    }
        //}
    }


    @Override
    public void onResume() {
        super.onResume();

        mPayButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        //TODO reinit button to pay
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View contentView = inflater.inflate(R.layout.fragment_payment_form, container, false);
        return contentView;
    }

    public void launchHostedPaymentPage(String forwardURLString) {

        final Bundle customThemeBundle = getArguments().getBundle(CustomTheme.TAG);
        ForwardWebViewActivity.start(getActivity(), forwardURLString, customThemeBundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //if (mAvatarGrid != null) {
        //    outState.putInt(KEY_SELECTED_AVATAR_INDEX, mAvatarGrid.getCheckedItemPosition());
        //} else {
        //    outState.putInt(KEY_SELECTED_AVATAR_INDEX, GridView.INVALID_POSITION);
        //}
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

       // if (mPlayer == null || edit) {
       //     view.findViewById(R.id.empty).setVisibility(View.GONE);
       //     view.findViewById(R.id.content).setVisibility(View.VISIBLE);
       //     initContentViews(view);
       //     initContents();
       // } else {
       //     final Activity activity = getActivity();
       //     CategorySelectionActivity.start(activity, mPlayer);
       //     activity.finish();
       // }

        super.onViewCreated(view, savedInstanceState);
        initContentViews(view);

        //TODO put this paymentPageRequest as args to get it.

    }

    private StateListDrawable makeSelector(CustomTheme theme) {
        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(ContextCompat.getColor(getActivity(), theme.getColorPrimaryDarkId())));
        res.addState(new int[]{}, new ColorDrawable(ContextCompat.getColor(getActivity(), theme.getColorPrimaryId())));
        return res;
    }

    private void initContentViews(View view) {

        final Bundle customThemeBundle = getArguments().getBundle(CustomTheme.TAG);

        CustomTheme theme = CustomTheme.fromBundle(customThemeBundle);

        mPayButton = (Button) view.findViewById(R.id.pay_button);
        mPayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPayButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                launchRequest();
            }
        });

        mPayButton.setTextColor(ContextCompat.getColor(getActivity(), theme.getTextColorPrimaryId()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mPayButton.setBackground(makeSelector(theme));
        }

        mProgressBar = (ProgressBar) view.findViewById(R.id.empty);

        mCardInfoLayout = (LinearLayout) view.findViewById(R.id.card_info_layout);
        //mDoneFab = (FloatingActionButton) view.findViewById(R.id.done);
        //mDoneFab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        switch (v.getId()) {
        //            case R.id.done:
        //                removeDoneFab(new Runnable() {
        //                    @Override
        //                    public void run() {
        //                        validateCartWithTransition();
        //                        mDoneFab.setVisibility(View.INVISIBLE);
        //                    }
        //                });
        //                break;
        //            default:
        //                throw new UnsupportedOperationException(
        //                        "The onClick method has not been implemented for " + getResources()
        //                                .getResourceEntryName(v.getId()));
        //        }
        //    }
        //});

        //TextWatcher textWatcher = new TextWatcher() {
        //    @Override
        //    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //        /* no-op */
        //    }

        //    @Override
        //    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //        // hiding the floating action button if text is empty
        //        if (s.length() == 0) {
        //            mDoneFab.hide();
        //        }
        //    }

        //    @Override
        //    public void afterTextChanged(Editable s) {
        //        // showing the floating action button if avatar is selected and input data is valid
        //        if (isAvatarSelected() && isInputDataValid()) {
        //            mDoneFab.show();
        //        }
            //}
        //};

        mCardOwner = (EditText) view.findViewById(R.id.card_owner);
        mCardOwner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* no-op */
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // hiding the floating action button if text is empty
                if (s.length() == 0) {
                    //mDoneFab.hide();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // showing the floating action button if avatar is selected and input data is valid
                if (isInputDataValid()) {
                    //mDoneFab.show();
                }
          }
        });

        mCardNumber = (EditText) view.findViewById(R.id.card_number);

//        InputFilter filter = new InputFilter() {
//            public CharSequence filter(CharSequence source, int start, int end,
//                                       Spanned dest, int dstart, int dend) {
//
//                for (int i = start; i < end; i++) {
//
//                    if (source.charAt(i) == ' ') {
//
//                        //return "k";
//                    }
//
//                }
                //return null;
            //}
        //};

        //mCardNumber.setFilters(new InputFilter[] { filter });

        mCardNumber.addTextChangedListener(new TextWatcher() {

            boolean addSlash = false;
            private static final char space = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                /* no-op */

                if (s.length() == 0) {
                    //mDoneFab.hide();
                }

                if (start == 4 && count == 0 && after == 1 && s.length() == 4) {
                    addSlash = true;
                } else {
                    addSlash = false;
                }

                if (start == 9 && count == 0 && after == 1 && s.length() == 4) {
                    addSlash = true;
                } else {
                    addSlash = false;
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // hiding the floating action button if text is empty
                if (s.length() == 0) {
                    //mDoneFab.hide();
                }

                if (start == 4 && before == 0 && count == 1 && s.length() == 5) {
                    addSlash = true;

                } else

                {
                    addSlash = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (isInputDataValid()) {
                    //mDoneFab.show();
                }

                if (addSlash == true) {

                    s.insert(4, " ");
                }

                addSlash = false;

                // Remove spacing char
                //if (s.length() > 0 && (s.length() % 5) == 0) {
                    //final char c = s.charAt(s.length() - 1);
                    //if (space == c) {
                        //s.delete(s.length() - 1, s.length());
                    //}
                //}
                //// Insert char where needed.
                //if (s.length() > 0 && (s.length() % 5) == 0) {
                    //char c = s.charAt(s.length() - 1);
                    //// Only if its a digit where there should be a space we insert a space
                    //if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                        //s.insert(s.length() - 1, String.valueOf(space));
                    //}
                //}
            }
        });

        mCardExpiration = (EditText) view.findViewById(R.id.card_expiration);
        mCardExpiration.addTextChangedListener(new TextWatcher() {

            boolean addSlash = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (s.length() == 0) {
                    //mDoneFab.hide();
                }

                StringBuilder stringBuilder = new StringBuilder("beforeText").append("\n");
                stringBuilder.append(" charseq ").append(s).append("\n")
                        .append(" start ").append(start).append("\n")
                        .append(" count ").append(count).append("\n")
                        .append(" after ").append(after);

                Log.i("go", stringBuilder.toString());

                if (start == 2 && count == 0 && after == 1 && s.length() == 2) {
                    addSlash = true;
                } else {
                    addSlash = false;
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) {
                    //mDoneFab.hide();
                }

                StringBuilder stringBuilder = new StringBuilder("onTextChanged").append("\n");
                stringBuilder.append(" charseq ").append(s).append("\n")
                        .append(" start ").append(start).append("\n")
                        .append(" before ").append(before).append("\n")
                        .append(" count ").append(count);

                Log.i("go", stringBuilder.toString());

                if (start == 2 && before == 0 && count == 1 && s.length() == 3) {
                    addSlash = true;
                } else {
                    addSlash = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (isInputDataValid()) {
                    //mDoneFab.show();
                }

                Log.i("go", s.toString());

                if (addSlash == true) {

                    s.insert(2, "/");
                    addSlash = false;
                }

                Log.i("after go", s.toString());
                //if (s.toString().equalsIgnoreCase("5")) {
                    //s.append("/");
                //}
            }
        });

        mCardCVV = (EditText) view.findViewById(R.id.card_cvv);
        mCardCVV.addTextChangedListener(new TextWatcher() {

            boolean addSlash = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) {
                    //mDoneFab.hide();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (isInputDataValid()) {
                    //mDoneFab.show();
                }

            }
        });
    }


    protected void cancelOperation() {

        if (mGatewayClient != null) {
            mGatewayClient.cancelOperation();
            mGatewayClient = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelOperation();
    }

    private boolean isInputDataValid() {
        return FormHelper.isInputDataValid(mCardNumber.getText(), mCardExpiration.getText(), mCardCVV.getText(), mCardOwner.getText());
    }
 }