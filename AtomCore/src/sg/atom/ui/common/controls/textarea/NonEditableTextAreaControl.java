package sg.atom.ui.common.controls.textarea;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.AbstractController;
import de.lessvoid.nifty.controls.ScrollPanel;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.elements.tools.TextBreak;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.spi.render.RenderFont;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The standard controller implementation of the Textarea. An Textarea is a
 * scrollpanel which can hold multiple lines of text. It is not dynamically
 * editable like a textfield. However, lines can be dynamically added through
 * methods, in which case the textarea will expand its scrollarea. It also
 * supports automatic line wrapping.
 */
public class NonEditableTextAreaControl extends AbstractController implements ITextArea {

    /**
     * The name of the panel where text is being displayed.
     */
    private static final String TEXTPANEL_NAME = "textpanel";
    /**
     * The name of the text element rendering the text in the textarea.
     */
    private static final String TEXT_NAME = "areatext";
    /**
     * The ID of the panel which implements scrolling through the textarea, as
     * defined in the control definition.
     */
    private static final String SCROLLPANEL_NAME = "textScrollpanel";
    /**
     * The reference to the panel holding text.
     */
    private Element m_textPanel;
    /**
     * The reference of the text element holding the text renderer of the text
     * area.
     */
    private Element m_text;
    /**
     * The reference to the panel which implements scrolling through the
     * textarea.
     */
    private ScrollPanel m_scrollPanel;
    /**
     * The reference to the renderer which renders the text in the textarea.
     */
    private TextRenderer m_textRenderer;
    /**
     * True, if the vertical scrollbar of the textarea should autoscroll.
     */
    private boolean m_autoScroll;
    /**
     * The original height of the text area.
     */
    private int m_originalHeight;
    private Nifty nifty;
    private Screen screen;
    private Element element;
    private Properties parameter;
    private Attributes controlDefinitionAttributes;

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties parameter, Attributes controlDefinitionAttributes) {
        super.bind(element);

        this.nifty = nifty;
        this.screen = screen;
        this.element = element;
        this.parameter = parameter;
        this.controlDefinitionAttributes = controlDefinitionAttributes;

        m_textPanel = element.findElementByName(TEXTPANEL_NAME);
        m_text = element.findElementByName(TEXT_NAME);
        m_scrollPanel = screen.findNiftyControl(SCROLLPANEL_NAME, ScrollPanel.class);
        m_textRenderer = m_text.getRenderer(TextRenderer.class);

        if (m_textRenderer == null) {
            return;
        }
        if (m_scrollPanel == null) {
            return;
        }
        initTextArea();
    }

    public void doBind(ScrollPanel scrollPanel) {
        m_textPanel = element.findElementByName(TEXTPANEL_NAME);
        m_text = element.findElementByName(TEXT_NAME);
        m_scrollPanel = scrollPanel;
        m_textRenderer = m_text.getRenderer(TextRenderer.class);
        initTextArea();
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        return false;
    }

    @Override
    public void appendLine(String text) {
        String[] wrappedText = this.wrapText(text.split("n", -1));
        int oldHeight = m_textRenderer.getTextHeight();

        for (String line : wrappedText) {
            String originalText = m_textRenderer.getOriginalText();

            if (!originalText.isEmpty()) {
                m_textRenderer.setText(originalText + "n" + line);
            } else {
                m_textRenderer.setText(line);
            }
        }

        if (m_textPanel.getHeight() < m_textRenderer.getTextHeight()) {
            m_textPanel.setConstraintHeight(new SizeValue(m_textRenderer.getTextHeight() + "px"));
            m_scrollPanel.getElement().layoutElements();

            if (m_autoScroll && (m_scrollPanel.getVerticalPos() == oldHeight - m_originalHeight || oldHeight - m_originalHeight < 0)) {
                m_scrollPanel.setVerticalPos(m_textRenderer.getTextHeight());
                m_scrollPanel.getElement().layoutElements();
            }
        }
    }

    @Override
    public void setAutoscroll(boolean autoScroll) {
        m_autoScroll = autoScroll;
    }

    @Override
    public void clearTextarea() {
        m_textRenderer.setText("");

        m_textPanel.setConstraintHeight(new SizeValue(m_originalHeight + "px"));
        m_scrollPanel.getElement().layoutElements();
    }

    /**
     * Wraps the given lines of a text into more lines, if they exceed the
     * maximum width of the textarea. Returns the wrapped lines in an array,
     * each element holding one line.
     *     
* @param textLines An array containing lines of a text which should be
     * wrapped.
     * @return An array containing the newly wrapped lines. If wrapping was not
     * necessary on any line, this contains the original textLines.
     */
    private String[] wrapText(final String[] textLines) {
        RenderFont font = m_textRenderer.getFont();

        List< String> lines = new ArrayList< String>();
        for (String line : textLines) {
            int lineLengthInPixel = font.getWidth(line);
            if (lineLengthInPixel > m_text.getWidth()) {
                lines.addAll(new TextBreak(line, m_text.getWidth(), font).split());
            } else {
                lines.add(line);
            }
        }
        return lines.toArray(new String[0]);
    }

    private void initTextArea() {
        m_textRenderer.setXoffsetHack(1);
        m_textRenderer.setLineWrapping(true);

        m_scrollPanel.setStepSizeY(12);
        m_scrollPanel.setPageSizeY(50);

        m_autoScroll = true;

        m_originalHeight = m_scrollPanel.getHeight();
    }
}