package com.example.shedule;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity
{
    String urlSite = "http://planwe.pollub.pl/plan.php?type=0&id=12791&winW=1904&winH=947&loadBG=000000";
    TextView[] mondayCells, tuesdayCells, wednesdayCells, thursdayCells, fridayCells;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExecutorService executor = Executors.newSingleThreadExecutor(); //new thread to perform async

        setCellsIds();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsDialog();
            }
        });

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            getContentFromWebsite(urlSite);
        }, executor);

        future.whenComplete((result, exception) -> {
            executor.shutdown();
        });
    }

    public void getContentFromWebsite(String url)
    {
        String colorBackground="";
        String courseName;
        String details;

        boolean connectionToSite=true;
        int left = 0, width=0;
        ProgressBar progressBar = findViewById(R.id.progressBar);
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        progressBar.setVisibility(View.VISIBLE); //loading
        mainLayout.setVisibility(View.INVISIBLE);


        try {
            Document document = Jsoup.connect(url).get();
            Elements divElements = document.select("div.coursediv");//take all classes from the plan


            for (Element divElement : divElements) {

                if (divElement.id().equals("testBox"))
                    continue; //last element in this elements is useless

                String styleAttribute = divElement.attr("style");
                String[] styleParts = styleAttribute.split(";");

                for (String part : styleParts)
                {
                    if (part.contains("left:")) {
                        left = Integer.parseInt(part.split(":")[1].trim().replace("px", ""));
                    } else if (part.contains("background-color:")) {
                        colorBackground = part.split(":")[1].trim();
                    }
                    else if (part.contains("width:")) {
                        width = Integer.parseInt(part.split(":")[1].trim().replace("px", ""));
                    }
                }

                //declare informations must be in this loop
                String[] informations  = new String[3]; //[0]=name, [1]=room, [2]=type of course and hours

                courseName = divElement.ownText().split(",")[0];
                details = divElement.ownText().substring(courseName.length() + 2);
                informations[2] = details;
                //hours to sort classes into individual cells
                String[] parts = details.split(" ");
                int startTime = 0;
                int endTime = 0;
                for (String part : parts)
                {
                    if(part.contains(":") && startTime==0)
                    {
                        startTime  = Integer.parseInt(part.substring(0, part.indexOf(":")));
                    }
                    else if(part.contains(":") && endTime==0)
                    {
                        endTime  = Integer.parseInt(part.substring(0, part.indexOf(":")));
                    }
                }

                //here are the rooms and names
                 Elements linkElements = divElement.select("a");

                int k=0;
                informations[0]="";//because of adding string in this
                informations[1]="";//because of adding string in this

                for(Element linkElement : linkElements)
                {
                    if(k==2) break;
                    if(informations[k].contains("type=20")) k++;
                    informations[k]+= "\n" + linkElement.text();
                }
                    if(informations[0]!=null)
                        informations[0] = informations[0].substring(1);


                    if(left>=88 && left<width+88) {
                        setCourse(mondayCells, startTime, courseName, colorBackground, informations);
                        setCourseWithoutName(mondayCells, startTime + 1, colorBackground);
                        if(startTime + 1!=endTime - 1)
                            setCourseWithoutName(mondayCells, endTime - 1, colorBackground);//-1 because of long courses(longer than 1 hour)
                    }
                    else if(left>=88 && left<88+width*2) {
                        setCourse(tuesdayCells, startTime, courseName, colorBackground, informations);
                        setCourseWithoutName(tuesdayCells, startTime + 1, colorBackground);
                        if(startTime + 1!=endTime - 1)
                            setCourseWithoutName(tuesdayCells, endTime - 1, colorBackground);//-1 because of long courses(longer than 1 hour)
                    }
                    else if(left>=88+width*2 && left<88+width*3) {
                        setCourse(wednesdayCells, startTime, courseName, colorBackground, informations);
                        setCourseWithoutName(wednesdayCells, startTime + 1, colorBackground);
                        if(startTime + 1!=endTime - 1)
                            setCourseWithoutName(wednesdayCells, endTime - 1, colorBackground);//-1 because of long courses(longer than 1 hour)
                    }
                    else if(left>=88+width*3 && left<88+width*4) {
                            setCourse(thursdayCells, startTime, courseName, colorBackground, informations);
                            setCourseWithoutName(thursdayCells, startTime + 1, colorBackground);
                            if(startTime + 1!=endTime - 1)
                                setCourseWithoutName(thursdayCells, endTime - 1, colorBackground);//-1 because of long courses(longer than 1 hour)
                        }
                    else if(left>=88+width*4 && left<88+width*5) {
                        setCourse(fridayCells, startTime, courseName, colorBackground, informations);
                        setCourseWithoutName(fridayCells, startTime + 1, colorBackground);
                        if(startTime + 1!=endTime - 1)
                            setCourseWithoutName(fridayCells, endTime - 1, colorBackground);//-1 because of long courses(longer than 1 hour)
                    }
                }
            }
        catch (Exception e)
        {
            connectionToSite=false;
            e.printStackTrace();
            toastMessage("Wystąpił problem podczas pobierania danych ze strony.");
        }

        if(connectionToSite) //if the app is ready to be displayed, loading screen is gone
        {
            toastMessage("Połączenie ze stroną udane!");

            runOnUiThread(() -> {
                mainLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            });
        }
    }

    public void setCellsIds()
    {
        mondayCells = new TextView[13];
        tuesdayCells = new TextView[13];
        wednesdayCells = new TextView[13];
        thursdayCells = new TextView[13];
        fridayCells = new TextView[13];

        for(int i=0; i<13; i++)
        {
            mondayCells[i] = findViewById(getResources().getIdentifier("m" + i, "id", getPackageName()));
            tuesdayCells[i] = findViewById(getResources().getIdentifier("t" + i, "id", getPackageName()));
            wednesdayCells[i] = findViewById(getResources().getIdentifier("w" + i, "id", getPackageName()));
            thursdayCells[i] = findViewById(getResources().getIdentifier("th" + i, "id", getPackageName()));
            fridayCells[i] = findViewById(getResources().getIdentifier("f" + i, "id", getPackageName()));
        }
    }
    public void setCourse(TextView[] cells, int hour, String courseName, String colorBackground, String[] informations)
    {
        switch(hour)
        {
            case 8:
                createCourse(cells[0], courseName, colorBackground, informations);
                break;
            case 9:
                createCourse(cells[1], courseName, colorBackground, informations);
                break;
            case 10:
                createCourse(cells[2], courseName, colorBackground, informations);
                break;
            case 11:
                createCourse(cells[3], courseName, colorBackground, informations);
                break;
            case 12:
                createCourse(cells[4], courseName, colorBackground, informations);
                break;
            case 13:
                createCourse(cells[5], courseName, colorBackground, informations);
                break;
            case 14:
                createCourse(cells[6], courseName, colorBackground, informations);
                break;
            case 15:
                createCourse(cells[7], courseName, colorBackground, informations);
                break;
            case 16:
                createCourse(cells[8], courseName, colorBackground, informations);
                break;
            case 17:
                createCourse(cells[9], courseName, colorBackground, informations);
                break;
            case 18:
                createCourse(cells[10], courseName, colorBackground, informations);
                break;
            case 19:
                createCourse(cells[11], courseName, colorBackground, informations);
                break;
            case 20:
                createCourse(cells[12], courseName, colorBackground, informations);
                break;
            default:
                break;
        }
    }

    public void setCourseWithoutName(TextView[] cells, int hour, String colorBackground)
    {
        switch(hour)
        {
            case 8:
                setCellBackground(cells[0], colorBackground);
                break;
            case 9:
                setCellBackground(cells[1], colorBackground);
                break;
            case 10:
                setCellBackground(cells[2], colorBackground);
                break;
            case 11:
                setCellBackground(cells[3], colorBackground);
                break;
            case 12:
                setCellBackground(cells[4], colorBackground);
                break;
            case 13:
                setCellBackground(cells[5], colorBackground);
                break;
            case 14:
                setCellBackground(cells[6], colorBackground);
                break;
            case 15:
                setCellBackground(cells[7], colorBackground);
                break;
            case 16:
                setCellBackground(cells[8], colorBackground);
                break;
            case 17:
                setCellBackground(cells[9], colorBackground);
                break;
            case 18:
                setCellBackground(cells[10], colorBackground);
                break;
            case 19:
                setCellBackground(cells[11], colorBackground);
                break;
            case 20:
                setCellBackground(cells[12], colorBackground);
                break;
            default:
                break;
        }
    }
    public void createCourse(TextView cell, String courseName, String colorBackground, String[] informations)
    {
        cell.setTextColor(Color.BLACK);
        cell.setText(courseName);
        cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                    showCourseInfoDialog("Szczegółowe informacje", informations[0] + informations[1] + "\n" + informations[2]);
            }
        });
        setCellBackground(cell, colorBackground);
    }

    public void setCellBackground(TextView cell, String colorBackground)
    {
        GradientDrawable background = (GradientDrawable) cell.getBackground();
        background.setColor(Color.parseColor(colorBackground));
    }

    public void showCourseInfoDialog(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    public void showSettingsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wprowadź link swojego planu:");

        //input from user
        EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String url = input.getText().toString();

                if(url.contains("http://planwe.pollub.pl/plan.php?type="))
                {
                    reloadLayout(url);
                }
                else toastMessage("Niepoprawny link do planu zajęć");
            }
        });

        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void toastMessage(String text)
    {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        });
    }

    public void reloadLayout(String url)
    {
        RelativeLayout mainLayout = findViewById(R.id.mm);
        mainLayout.removeAllViews();
        ViewGroup newLayout = inflateLayout(R.layout.activity_main);
        mainLayout.addView(newLayout);

        setCellsIds();

        //reseting cells background color because inflateLayout doeasnt always work on cells background color
        for(int i=0; i<13; i++)
        {
            ((GradientDrawable) mondayCells[i].getBackground()).setColor(Color.rgb(224, 224, 224));
            ((GradientDrawable) tuesdayCells[i].getBackground()).setColor(Color.rgb(224, 224, 224));
            ((GradientDrawable) wednesdayCells[i].getBackground()).setColor(Color.rgb(224, 224, 224));
            ((GradientDrawable) thursdayCells[i].getBackground()).setColor(Color.rgb(224, 224, 224));
            ((GradientDrawable) fridayCells[i].getBackground()).setColor(Color.rgb(224, 224, 224));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsDialog();
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor(); //new thread to perform async

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            getContentFromWebsite(url);
        }, executor);

        future.whenComplete((result, exception) -> {
            executor.shutdown();
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainLayout.invalidate();
            }
        });
    }

    private ViewGroup inflateLayout(int layoutResId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        return (ViewGroup) inflater.inflate(layoutResId, null);
    }
}

