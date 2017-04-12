/**
 * Created by gizem on 12.04.2017.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonModel;
import javax.swing.JButton;

/**
 *
 * @author Dhilshuk Reddy
 *
 */
public class GlossyButton extends JButton {

    private int buttonTheme = Theme.SILVER_THEME;
    private int rollOverTheme = Theme.GLOSSY_SILVER_THEME;
    private int selectedTheme = Theme.GLOSSY_GREEN_THEME;;
    private String buttonType = ButtonType.BUTTON_ROUNDED_RECTANGLUR;
    private GradientPaint[] glossyColors = new GradientPaint[2];
    private GradientPaint glossyBgColor;
    private GradientPaint glossyFgColor;

    /**
     * Constructor which sets label of the button.
     *
     * @param text
     *            label on the button
     */
    public GlossyButton(String text) {
        super(text);
        init();
    }

    /**
     * Constructor which sets the label and theme for the button.
     *
     * @param text
     *            label on the button
     * @param buttonTheme
     *            button theme.
     */
    public GlossyButton(String text, int buttonTheme) {
        super(text);
        this.buttonTheme = buttonTheme;
        init();
    }

    /**
     * Constructor which sets the label and type for the button.
     *
     * @param text
     *            label on the button
     * @param buttonType
     *            shape of the button
     */
    public GlossyButton(String text, String buttonType) {
        super(text);
        this.buttonType = buttonType;
        init();
    }

    /**
     * Constructor which sets the label,type and theme for the button.
     *
     * @param text
     *            label on the button
     * @param buttonTheme
     *            theme of the button
     * @param buttonType
     *            shape of the button
     */
    public GlossyButton(String text, int buttonTheme, String buttonType) {
        super(text);
        this.buttonTheme = buttonTheme;
        this.buttonType = buttonType;
        init();
    }

    /**
     * Constructor which sets the label,type,theme and roll-over theme for the
     * button.
     *
     * @param text
     *            label on the button
     * @param buttonType
     *            shape of the button
     * @param buttonTheme
     *            theme of the button
     * @param rolloverTheme
     *            roll-over theme
     */
    public GlossyButton(String text, String buttonType, int buttonTheme,
                        int rolloverTheme) {
        super(text);
        this.buttonType = buttonType;
        this.buttonTheme = buttonTheme;
        this.rollOverTheme = rolloverTheme;
        init();
    }

    /**
     * Constructor which sets the label,type,theme ,roll-over and selected theme
     * for the button.
     *
     * @param text
     *            label on the button
     * @param buttonType
     *            shape of the button
     * @param buttonTheme
     *            theme of the button
     * @param rolloverTheme
     *            roll-over theme
     * @param selectedTheme
     *            selected theme
     */
    public GlossyButton(String text, String buttonType, int buttonTheme,
                        int rolloverTheme, int selectedTheme) {
        super(text);
        this.buttonTheme = buttonTheme;
        this.buttonType = buttonType;
        this.rollOverTheme = rolloverTheme;
        this.selectedTheme = selectedTheme;
        init();
    }

    /**
     * Initializes
     */
    private void init() {
        setFont(new Font("Thoma", Font.BOLD, 12));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        int w = getWidth();
        int height = getHeight();

        ButtonModel model = getModel();
        if (model.isRollover()) {
            glossyColors = ColorUtils.getInStance().getGlossyColor(
                    rollOverTheme, height, this);
        } else {
            glossyColors = ColorUtils.getInStance().getGlossyColor(buttonTheme,
                    height, this);

        }
        if (model.isSelected() || model.isPressed()) {
            glossyColors = ColorUtils.getInStance().getGlossyColor(
                    selectedTheme, height, this);

        }
        glossyBgColor = glossyColors[1];
        glossyFgColor = glossyColors[0];
        drawShape(g2d, w, h);
        g2d.dispose();
        super.paintComponent(g);

    }

    /**
     * Draws the shape.
     *
     * @param g2d
     *            2D Graphics object.
     * @param w
     *            width of the button
     * @param h
     *            height of the Button
     */
    private void drawShape(Graphics2D g2d, int w, int h) {
        if (buttonType == ButtonType.BUTTON_ROUNDED_RECTANGLUR) {
            RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0,
                    w - 1, h - 1, 8, 8);
            Shape clip = g2d.getClip();
            g2d.clip(r2d);
            g2d.setPaint(glossyBgColor);
            g2d.fillRoundRect(0, 0, w, h, 8, 8);
            g2d.setClip(clip);
            g2d.setPaint(glossyFgColor);
            g2d.fillRoundRect(2, 2, w - 4, h / 2, 5, 5);

            g2d.setColor(new Color(50, 50, 50, 200));
            g2d.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);
        } else if (buttonType == ButtonType.BUTTON_RECTANGULAR) {

            g2d.setPaint(glossyColors[1]);
            g2d.fillRect(0, 0, w, h);

            g2d.setPaint(glossyColors[0]);
            g2d.fillRect(2, 2, w - 4, h / 2);

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawRect(0, 0, w - 1, h - 1);
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRect(1, 1, w - 3, h - 3);

        } else if (buttonType == ButtonType.BUTTON_ROUNDED) {

            RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0,
                    w - 1, h - 1, 8, 8);
            Shape clip = g2d.getClip();
            g2d.clip(r2d);

            g2d.setPaint(glossyBgColor);
            g2d.fillRoundRect(0, 0, w, h, h - 3, h - 3);
            g2d.setClip(clip);

            g2d.setPaint(glossyFgColor);
            g2d.fillRoundRect(2, 2, w - 4, h / 2, h - 5, h - 5);

            g2d.setColor(new Color(100, 100, 100));
            g2d.drawRoundRect(0, 0, w - 1, h - 1, h - 3, h - 3);
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(1, 1, w - 3, h - 3, h - 3, h - 3);

        }
    }

    /**
     * Returns Button Theme.
     *
     * @return button theme
     */
    public int getButtonTheme() {
        return buttonTheme;
    }

    /**
     * Sets button theme.
     *
     * @param buttonTheme
     */
    public void setButtonTheme(int buttonTheme) {
        this.buttonTheme = buttonTheme;
    }

    /**
     * Returns roll-over theme
     *
     * @return roll over theme.
     */
    public int getRollOverTheme() {
        return rollOverTheme;
    }

    /**
     * Sets the roll over theme.
     *
     * @param rollOverTheme
     */
    public void setRollOverTheme(int rollOverTheme) {
        this.rollOverTheme = rollOverTheme;
    }

    /**
     * Returns the selected theme.
     *
     * @return selected Theme.
     */
    public int getSelectedTheme() {
        return selectedTheme;
    }

    /**
     * Sets the selected theme.
     *
     * @param selectedTheme
     */
    public void setSelectedTheme(int selectedTheme) {
        this.selectedTheme = selectedTheme;
    }
    public class ButtonType {

        public static final String BUTTON_ROUNDED_RECTANGLUR = "Rounded Rectangle";
        public static final String BUTTON_RECTANGULAR = "Rectangle";
        public static final String BUTTON_OVAL = "Oval";
        public static final String BUTTON_CIRCULAR = "Circle";
        public static final String BUTTON_ARROW = "Arrow";
        public static final String BUTTON_ELLIPSE = "Ellipse";
        public static final String BUTTON_ROUNDED = "Rounded";

    }
}