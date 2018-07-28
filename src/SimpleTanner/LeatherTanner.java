package SimpleTanner;

import SimpleTanner.Tasks.BankLeatherWithdrawCowhide;
import SimpleTanner.Tasks.TanHide;
import SimpleTanner.Tasks.WalkToBank;
import SimpleTanner.Tasks.WalkToTanner;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.text.DecimalFormat;
import java.time.Duration;

@ScriptMeta(name = "Best Leather Tanner", developer = "codekiwi", desc = "Tans Cowhide into Leather", category = ScriptCategory.MONEY_MAKING, version = 1)
public class LeatherTanner extends TaskScript implements RenderListener, ImageObserver {
    public static final int COWHIDE = 1739;
    public static final Area TANNER_AREA = Area.rectangular(3271, 3191, 3277, 3193);

    private final Task[] TASKS = {
        new WalkToBank(),
        new BankLeatherWithdrawCowhide(this),
        new WalkToTanner(),
        new TanHide(this)
    };

    public int totalTanned = 0;
    private final int leatherPrice = FetchHelper.fetchItemPrice("https://api.rsbuddy.com/grandExchange?a=guidePrice&i=1741", 115);
    private final int cowhidePrice = FetchHelper.fetchItemPrice("https://api.rsbuddy.com/grandExchange?a=guidePrice&i=1739", 70);

    StopWatch timeRan = null; // stopwatch is started by GUI

    @Override
    public void onStart() {
        SimpleTannerGUI gui = new SimpleTannerGUI(this);
        gui.setVisible(true);
    }

    @Override
    public int loop() {
        for (Task task : TASKS) {
            if (task.validate()) {
                return task.execute();
            }
        }
        return 600;
    }

    @Override
    public void onStop() {
        this.logStats();
    }

    private int getHourlyRate(Duration sw) {
        double hours = sw.getSeconds() / 3600.0;
        double tannedPerHour = this.totalTanned / hours;
        return (int) tannedPerHour;
    }

    private void logStats() {
        int[] stats = this.getStats();
        String statsString = "Tanned: "
            + stats[0]
            + "  |  Total profit: " + stats[1]
            + "  |  Hourly profit: " + stats[2];
        Log.info(statsString);
    }

    // painting
    private static final Font runescapeFont = new Font("RuneScape Small", Font.PLAIN, 24).deriveFont(32f);
    private static final Font runescapeFontSmaller = new Font("RuneScape Small", Font.PLAIN, 24);
    private static final DecimalFormat formatNumber = new DecimalFormat("#,###");
    private static final String imageUrl = "https://i.imgur.com/55MEmwU.png";
    private static final Image image1 = FetchHelper.getImage(imageUrl);

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    @Override
    public void notify(RenderEvent renderEvent) {
        Graphics g = renderEvent.getSource();

        // render the paint layout
        g.drawImage(LeatherTanner.image1, 0, 0, this);

        // render time running
        g.setFont(runescapeFontSmaller);
        this.drawStringWithShadow(
                g,
                this.timeRan == null ? "00:00:00" : this.timeRan.toElapsedString(),
                238,
                24,
                Color.YELLOW.darker()
        );

        // render tanned and profit
        int[] stats = this.getStats();
        int totalCowhideTanned = stats[0];
        int totalProfit = stats[1];
        int hourlyProfit = stats[2];

        g.setFont(runescapeFont);

        this.drawStringWithShadow(g, formatNumber.format(totalCowhideTanned), 68 ,229, Color.YELLOW);
        this.drawStringWithShadow(g, formatNumber.format(totalProfit), 68 ,274, Color.WHITE);
        this.drawStringWithShadow(g, formatNumber.format(hourlyProfit), 68 ,319, Color.WHITE);
    }

    private void drawStringWithShadow(Graphics g, String str, int x, int y, Color color) {
        g.setColor(Color.BLACK);
        g.drawString(str, x + 2, y + 2); // draw shadow
        g.setColor(color);
        g.drawString(str, x, y); // draw string
    }

    private int[] getStats() {
        final Duration durationRunning = this.timeRan == null ? Duration.ofSeconds(0) : this.timeRan.getElapsed();

        int totalLeatherValue = this.totalTanned * this.leatherPrice;
        int totalProfit = totalLeatherValue - this.totalTanned * this.cowhidePrice;
        int hourlyProfit = this.getHourlyRate(durationRunning) * (this.leatherPrice - this.cowhidePrice);
        int[] stats = {
                this.totalTanned,
                totalProfit,
                hourlyProfit
        };
        return stats;
    }
}