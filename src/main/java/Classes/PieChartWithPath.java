package Classes;

import javafx.animation.PathTransition;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class PieChartWithPath {

    private int dataCount;     // The number of data values for the chart.
    private ObservableList<Data> dataList;
    private Group group;

    private int[] angles;

    private final static Color[] palette = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.CYAN
    };


    public PieChartWithPath(ObservableList<Data> data, Group root) {
        dataList = data;
        dataCount = dataList.size();
        this.group = root;
        setData();
    }

    // sets angles of Arcs
    void setData() {
        angles = new int[dataCount + 1];
        angles[0] = 0;
        angles[dataCount] = 360;
        double dataSum = 0;
        for (Data node: dataList) {
            dataSum += node.getPieValue();
        }
        double sum = 0;
        for (int i = 1; i < dataCount; i++) {
            sum += dataList.get(i-1).getPieValue();
            angles[i] = (int)(360*sum/dataSum + 0.5);
        }
    }


    public Group paint() {
        if (dataCount == 0) {
            System.out.println("No data available in method Paint.");
            return null;
        }

        int centerX = 230;   // Center point of circle.
        int centerY = 220;

        int radius = centerY - 20;
        double previousXofArc = centerX + radius;
        double previousYofArc = centerY + 0;


        int j = 1;
        for (Data node: dataList) {
            double currentAngleRadians = Math.toRadians(angles[j]);
            double currentXofArc = centerX + radius*Math.cos(currentAngleRadians);
            double currentYofArc = centerY + radius*Math.sin(currentAngleRadians);
            Path arcPath = new Path(
                    new MoveTo(previousXofArc, previousYofArc),
                    new ArcTo(radius,radius, 25, currentXofArc, currentYofArc, false, true),
                    // line from center to first point of arc
                    new MoveTo(centerX, centerY),
                    new LineTo(previousXofArc, previousYofArc),
                    // line from center to second point of arc
                    new MoveTo(centerX, centerY),
                    new LineTo(currentXofArc, currentYofArc)
            );
            arcPath.setStrokeWidth(2);
            arcPath.setStroke(palette[j % 6]);
            group.getChildren().add(arcPath);
            previousXofArc = currentXofArc;
            previousYofArc = currentYofArc;
            j++;

        }

        return group;
    }



}