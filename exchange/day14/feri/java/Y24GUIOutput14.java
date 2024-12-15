import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicTextUI.BasicCaret;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


class Y24GUIOutput14 {

	@SuppressWarnings("serial") public static class MyCaret extends BasicCaret {
		@Override public void install(JTextComponent c) { return; }
	}

	// https://stackoverflow.com/questions/7156038/how-to-turn-off-jtextpane-line-wrapping
	@SuppressWarnings("serial") public static class JTextPaneNowrap extends JTextPane {
		@Override public boolean getScrollableTracksViewportWidth() {  return getPreferredSize().width <= getParent().getSize().width; }
	    @Override public Dimension getPreferredSize() {  return getUI().getPreferredSize(this); };		
	}
	
	private List<String> textListe;
	private int currentText;

    boolean buffered;

    JFrame f;
    JTextPaneNowrap jt;
    JScrollPane sp;
    JLabel lbTextID;
    JSlider slider;
    
    Timer timer;
    
    public Y24GUIOutput14(String title, boolean buffered) {
    	this.textListe = new ArrayList<>();
    	this.currentText = -1;
    	this.buffered = buffered;
		create(title);
	}
    
    public void create(String title) {
    	initColorStlye();
        f = new JFrame(title);
        Container p = f.getContentPane();
        
        BoxLayout bl = new BoxLayout(p, BoxLayout.Y_AXIS);
        p.setLayout(bl);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        JButton btPrevious = new JButton("<");
        buttonPanel.add(btPrevious);
        btPrevious.addActionListener(ev -> {
        	previous();
        });

        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        
        lbTextID = new JLabel("0");
        buttonPanel.add(lbTextID);
        
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        
        JButton btNext = new JButton(">");
        buttonPanel.add(btNext);
        btNext.addActionListener(ev -> {
        	next();
        });
        
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));

        JButton btSmaller = new JButton("v");
        buttonPanel.add(btSmaller);
        btSmaller.addActionListener(ev -> {
        	smaller();
        });
        
        JButton btBigger = new JButton("^");
        buttonPanel.add(btBigger);
        btBigger.addActionListener(ev -> {
        	bigger();
        });
        
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));

        JButton btBuffered = new JButton("Buffered");
        buttonPanel.add(btBuffered);
        btBuffered.addActionListener(ev -> {
        	buffered = ! buffered;
        });
        
        buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));
        
        JButton btAnimation = new JButton("Animation");
        buttonPanel.add(btAnimation);
        btAnimation.addActionListener(ev -> {
        	animation();
        });
        
        p.add(buttonPanel);

        slider = new JSlider(JSlider.HORIZONTAL, 0, 10000, 0);
        slider.addChangeListener(ev -> {
        	double percent = slider.getValue() * 0.0001;
        	int page = (int) (percent * (textListe.size()-1));
        	switchPage(page);
        });
        p.add(slider);
        
        jt = new JTextPaneNowrap();
        jt.setCaret(new MyCaret());
        jt.setEditable(false);
//        jt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        jt.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        sp = new JScrollPane(jt); 
        p.add(sp);
        
        // set initial size of frame
        f.setSize(800, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    
    private int nextPage = -1;

    private synchronized void switchPage(int page) {
    	if (nextPage == -1) {
        	nextPage = page;
        	SwingUtilities.invokeLater(() -> asyncSwitchPage());
        	return;
    	}
    	nextPage = page;
    }

    private Map<String, AttributeSet> styles = new HashMap<>();
    private AttributeSet col(String name) { return styles.get(name); }
    private void addColorStyle(Color c, String... names) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        for (String name:names) {
        	styles.put(name, aset);
        }
    }
    private void addBackgroundStyle(Color cBackground, Color cForeground, String... names) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, cForeground);
        aset = sc.addAttribute(aset, StyleConstants.Background, cBackground);
        for (String name:names) {
        	styles.put(name, aset);
        }
    }
    
    private void initColorStlye() {
    	addColorStyle(Color.BLACK,      "cblack",     "cbk", "c0" );
    	addColorStyle(Color.RED,        "cred",       "cre", "c1" );
    	addColorStyle(Color.GREEN,      "cgreen",     "cgr", "c2" );
    	addColorStyle(Color.BLUE,       "cblue",      "cbl", "c3" );
    	addColorStyle(Color.CYAN,       "ccyan",      "ccy", "c4" );
    	addColorStyle(Color.MAGENTA,    "cmagenta",   "cma", "c5" );
    	addColorStyle(Color.YELLOW,     "cyellow",    "cye", "c6" );
    	addColorStyle(Color.ORANGE,     "corange",    "cor", "c7" );
    	addColorStyle(Color.PINK,       "cpink",      "cpi", "c8" );
    	addColorStyle(Color.DARK_GRAY,  "cdarkgray",  "cdg", "c9" );
    	addColorStyle(Color.GRAY,       "cgray",      "cgy", "c10");
    	addColorStyle(Color.LIGHT_GRAY, "clightgray", "clg", "c11");
    	addColorStyle(Color.WHITE,      "cwhite",     "cwh", "c12");
    	
    	addBackgroundStyle(Color.WHITE,  Color.BLACK,  "bwhite",  "bwh", "b0");
    	addBackgroundStyle(Color.BLACK,  Color.WHITE,  "bblack",  "bbl", "b1");
    	addBackgroundStyle(Color.YELLOW, Color.BLACK,  "byellow", "bye", "b2");
    	addBackgroundStyle(Color.RED,    Color.WHITE,  "bred",    "bre", "b3");
    	addBackgroundStyle(Color.ORANGE, Color.BLACK,  "borange", "bor", "b4");
    }
    
    private void appendToPane(JTextPane tp, String msg, AttributeSet aset) {
        StyledDocument sDoc = tp.getStyledDocument();
        try {
			sDoc.insertString(sDoc.getLength(), msg, aset);
		} catch (BadLocationException e) {
			throw new RuntimeException(e.toString(), e);
		}
    }
    
    private synchronized void asyncSwitchPage() {
    	int targetPage = Math.min(textListe.size()-1, Math.max(0, nextPage));
    	if (currentText != targetPage) {
    		currentText = targetPage;
    		String text = textListe.get(currentText);
    		sp.getHorizontalScrollBar().getValue();
    		sp.getVerticalScrollBar().getValue();
        	addColoredText(text);
        	lbTextID.setText(Integer.toString(currentText));   	
    	}
    	nextPage = -1;
	}
    
    
    private void addColoredText(String text) {
    	jt.setText("");
    	if (text.startsWith("°")) {
    		text = text.substring(1);
    	}
    	else {
    		text = "c0;"+text;
    	}
    	String[] coloredBlocks = text.split("°");
    	for (String coloredBlock:coloredBlocks) {
    		int pos = coloredBlock.indexOf(";");
    		if (pos == -1) {
    			System.err.println("[INVALID COLOR]: "+coloredBlock);
    			appendToPane(jt, "[INVALID COLOR]: "+coloredBlock, col("c0"));
    			return;
    		}
    		AttributeSet aset = col(coloredBlock.substring(0, pos));
    		if (aset == null) {
    			System.err.println("[INVALID COLOR]: "+coloredBlock);
    			appendToPane(jt, "[INVALID COLOR]: "+coloredBlock, col("c0"));
    			return;
    		}
        	appendToPane(jt, coloredBlock.substring(pos+1), aset);
    	}
	}

	private void previous() {
		switchPage(currentText-1);
	}
    
    private void next() {
		switchPage(currentText+1);
	}

	public void addStep(String text) {
		if (buffered) {
			textListe.add(text);
			if (currentText != -1) {
				return;
			}
		}
		currentText = textListe.size();
		textListe.add(text);
    	addColoredText(text);
    	lbTextID.setText(Integer.toString(currentText));
    }

    private void smaller() {
    	Font font = jt.getFont();
    	Font smallFont = font.deriveFont(font.getSize2D()/1.1f);
    	jt.setFont(smallFont);
	}

    private void bigger() {
    	Font font = jt.getFont();
    	Font smallFont = font.deriveFont(font.getSize2D()*1.1f);
    	jt.setFont(smallFont);
	}

    private synchronized void animation() {
    	if (timer != null) {
    		timer.stop();
    		timer = null;
    		return;
    	}
    	timer = new Timer(250, ev -> {
    		asyncAnimation();
    	});
    	timer.setInitialDelay(250);
    	timer.start();
	}

	private synchronized void asyncAnimation() {
		if (timer == null) {
			return;
		}
		int page = currentText + 1;
		if (page >= textListe.size()) {
			timer.stop();
			timer = null;
			return;
		}
		switchPage(page);
	}
    
	public String color(String colname) {
		return "°c"+colname+";";
	}

	public String style(String colname) {
		return "°"+colname+";";
	}

	public String plainText(String coloredText) {
		return coloredText.replaceAll("°c[a-z0-9]+;", "");
	}


	public static void main(String[] args) throws Exception {
		Y24GUIOutput14 output = new Y24GUIOutput14("title", true);
		output.addStep("Depending on the font size, the third line is longer or smaller than the upper two lines:\n\n"
				+ "°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X°c0;X°c1;X\n"
				+ "°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X°c0;X\n"
				+ "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
			);

		output.addStep("etwas°cblue; länger °byellow;eine°c0; lange °bblack;Zeile°c0;\netwas länger undeinekurze\netwas länger undeinekurze xx yy zz");
		output.addStep("°cred;etwas°cblue; länger °byellow;eine°c0; lange °bblack;Zeile°c0;\netwas länger undeinekurze\netwas länger undeinekurze xx yy zz");
//		Thread.sleep(1);
//    	SwingUtilities.invokeLater(() -> 
//    		output.jt.getPreferredSize()
//    	);
//		Thread.sleep(1);
	}

	
}