package cn.bong.bonghome;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.hackill.bong.Content;
import cn.hackill.bong.CycProgressBar;


/**
 * @author hackill
 * @date on 17/7/25 17:15
 */

public class TestActivity extends Activity {


    CycProgressBar cycProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test);

        cycProgressBar = (CycProgressBar) findViewById(R.id.cycbar);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float progress = cycProgressBar.getProgress();

                cycProgressBar.setProgress(progress + 1);
            }
        });

        findViewById(R.id.less).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float progress = cycProgressBar.getProgress();
                cycProgressBar.setProgress(progress - 1);
            }
        });


        findViewById(R.id.all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Content content = new Content();

                content.addition = "233";
                content.additionUnit = "大卡";
                content.value = "9999";
                content.unit = "步";
                content.imgRes = R.drawable.icon_sleep;

                cycProgressBar.setContent(content, CycProgressBar.Style.Sleep);

            }
        });

        findViewById(R.id.simple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Content content = new Content();

                content.value = "23120";
                content.unit = "步";
                cycProgressBar.setContent(content, CycProgressBar.Style.Thumbnail);
            }
        });

    }
}
