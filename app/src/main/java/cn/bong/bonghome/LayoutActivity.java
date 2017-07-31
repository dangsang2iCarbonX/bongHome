package cn.bong.bonghome;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;


/**
 * @author hackill
 * @date on 17/7/25 17:15
 */

public class LayoutActivity extends Activity {

    FrameLayout content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.framlayout);


        content = findViewById(R.id.layout);

        Button button = new Button(getApplicationContext());


        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 230);


        button.setLayoutParams(params);

        button.setBackground(getResources().getDrawable(R.color.sleep));

        content.addView(button);


        Button button2 = new Button(getApplicationContext());


        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180);


        button2.setLayoutParams(params2);

        button2.setBackground(getResources().getDrawable(R.color.bong_color));

        content.addView(button2);


    }
}
