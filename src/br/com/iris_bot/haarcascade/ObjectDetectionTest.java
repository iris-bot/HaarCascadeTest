package br.com.iris_bot.haarcascade;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class ObjectDetectionTest {

	private static final String[] imgFileExtensions = new String[] { "bmp", "jpg", "png", "jpeg", "gif" };
	private static final String[] xmlFileExtensions = new String[] { "xml" };

	private static HaarModel[] models = {
		new HaarModel("CV_HAAR_DO_CANNY_PRUNING", CV_HAAR_DO_CANNY_PRUNING),
		new HaarModel("CV_HAAR_DO_ROUGH_SEARCH", CV_HAAR_DO_ROUGH_SEARCH),
		new HaarModel("CV_HAAR_MAGIC_VAL", CV_HAAR_MAGIC_VAL),
		new HaarModel("CV_HAAR_SCALE_IMAGE", CV_HAAR_SCALE_IMAGE),
		new HaarModel("CV_HAAR_FEATURE_MAX", CV_HAAR_FEATURE_MAX),
		new HaarModel("CV_HAAR_FIND_BIGGEST_OBJECT", CV_HAAR_FIND_BIGGEST_OBJECT),
		new HaarModel("CV_HARDWARE_MAX_FEATURE", CV_HARDWARE_MAX_FEATURE)
	};
	
	private static JFileChooser fc = new JFileChooser();
	
	private File xml = new File("res/haarcascade_frontalface_alt.xml");
	private File img = new File("test/test1.jpg");
	private ImagePane image = new ImagePane(cvLoadImage(img.getAbsolutePath()).getBufferedImage());
	private JComboBox<HaarModel> model = new JComboBox<HaarModel>(models);
	private JSlider scale = new JSlider(11, 80);
	private JSlider neighbors = new JSlider(-1, 10);
	private JSlider minSize = new JSlider(20, 290);
	private JSlider maxSize = new JSlider(20, 290);
	private JButton btImg = new JButton("Selecionar Imagem");
	private JButton btXml = new JButton("Selecionar HaarCascade XML");
	private JLabel props = new JLabel("");
	
	private static DecimalFormat nf = new DecimalFormat("0.#");
	
	private static boolean processing = false;
	
	public static void main(String[] args) {

		final JFrame frm = new JFrame("Haar Cascade Object Detection Test");
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setMinimumSize(new Dimension(700, 700));
		final ObjectDetectionTest cd = new ObjectDetectionTest();
		cd.setup(cd.scale);
		cd.setup(cd.neighbors);
		
		cd.minSize.setOrientation(JSlider.VERTICAL);
		cd.maxSize.setOrientation(JSlider.VERTICAL);
		cd.setup(cd.minSize);
		cd.setup(cd.maxSize);

		frm.setContentPane(cd.getPanel());
		frm.setVisible(true);

		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);

		cd.btImg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setFileFilter(new ImageFileFilter());
				int res = fc.showOpenDialog(frm);
				if (res == JFileChooser.APPROVE_OPTION) {
					cd.img = fc.getSelectedFile();
					cd.detect();
				}

			}
		});

		cd.btXml.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setFileFilter(new XmlFileFilter());
				int res = fc.showOpenDialog(frm);
				if (res == JFileChooser.APPROVE_OPTION) {
					cd.xml = fc.getSelectedFile();
					cd.detect();
				}

			}
		});

		cd.scale.setValue(13);
		cd.neighbors.setValue(2);
		cd.minSize.setValue(80);
		cd.maxSize.setValue(160);
		
		cd.model.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cd.detect();
			}
		});

		cd.minSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!cd.minSize.getValueIsAdjusting()){
					cd.maxSize.setMinimum(cd.minSize.getValue()+1);
					cd.setup(cd.maxSize);
					cd.detect();
				}
			}
		});

		cd.maxSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!cd.maxSize.getValueIsAdjusting()){
					cd.minSize.setMaximum(cd.maxSize.getValue()-1);
					cd.setup(cd.minSize);
					cd.detect();
				}
			}
		});

		cd.scale.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!cd.scale.getValueIsAdjusting()) cd.detect();
			}
		});

		cd.neighbors.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!cd.neighbors.getValueIsAdjusting()) cd.detect();
			}
		});
		
		cd.detect();
	}

	public void detect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(!processing){
					setProcessing(true);
					double scl = ((double) scale.getValue()) / 10d;
					int nbr = neighbors.getValue();
					int min = minSize.getValue();
					int max = maxSize.getValue();
					int mdl = ((HaarModel)model.getSelectedItem()).value;
					props.setText("scale: "+nf.format(scl)+" | neighbors: "+nbr+" | size: "+min+"/"+max);
					image.setImage(detect(xml, cvLoadImage(img.getAbsolutePath()), scl, nbr, min, max, mdl).getBufferedImage());
					setProcessing(false);
				}
			}
		}).start();
	}

	private synchronized void setProcessing(boolean b){
		neighbors.setEnabled(!b);
		scale.setEnabled(!b);
		minSize.setEnabled(!b);
		maxSize.setEnabled(!b);
		model.setEnabled(!b);
		btImg.setEnabled(!b);
		btXml.setEnabled(!b);
		processing = b;
	}
		
	public JPanel getPanel() {
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		main.add(image, BorderLayout.CENTER);

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		JPanel top1 = new JPanel();
		JPanel top2 = new JPanel();
		top1.setLayout(new FlowLayout());
		top2.setLayout(new FlowLayout());
		top1.add(model);
		top1.add(props);
		top2.add(btXml);
		top2.add(btImg);
		top.add(top1, BorderLayout.NORTH);
		top.add(top2, BorderLayout.SOUTH);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(scale, BorderLayout.CENTER);
		bottom.add(neighbors, BorderLayout.SOUTH);

		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(minSize, BorderLayout.WEST);
		right.add(maxSize, BorderLayout.EAST);

		main.add(top, BorderLayout.NORTH);
		main.add(bottom, BorderLayout.SOUTH);
		main.add(right, BorderLayout.EAST);
		return main;
	}

	public void setup(JSlider jsl) {
		jsl.setMajorTickSpacing((int) Math.ceil(((double)(jsl.getMaximum()-jsl.getMinimum()))/10d));
		jsl.setMinorTickSpacing((int) Math.ceil(((double)(jsl.getMaximum()-jsl.getMinimum()))/100d));
		jsl.setPaintLabels(true);
		jsl.setPaintTicks(true);
		jsl.setPaintTrack(true);
		jsl.setSnapToTicks(true);
		jsl.setForeground(Color.BLACK);
	}

	public static IplImage detect(File xml, IplImage src, double scale, int neighbors, int min, int max, int model) {

		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(xml.getAbsolutePath()));
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(src, cascade, storage, scale, neighbors, model, cvSize(min, min), cvSize(max, max));

		cvClearMemStorage(storage);

		int total_caps = sign.total();
		System.out.println("Found: "+total_caps);

		for (int i = 0; i < total_caps; i++) {
			CvRect r = new CvRect(cvGetSeqElem(sign, i));
			cvRectangle(src, cvPoint(r.x(), r.y()), cvPoint(r.width() + r.x(), r.height() + r.y()), CvScalar.RED, 2,
					CV_AA, 0);

		}

		// cvShowImage("Result", src);
		// cvWaitKey(0);
		return src;

	}

	private static class ImagePane extends JPanel {
		private BufferedImage img;

		private ImagePane(BufferedImage img) {
			this.img = img;
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D graphics2d = (Graphics2D) g;
			graphics2d.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), 0, 0, img.getWidth(), img.getHeight(),
					null);
			super.paintComponents(g);
		}

		public void setImage(BufferedImage img) {
			this.img = img;
			repaint();
		}

	}

	private static class ImageFileFilter extends FileFilter {
		@Override
		public String getDescription() {
			return "image";
		}

		@Override
		public boolean accept(File file) {
			for (String extension : imgFileExtensions) {
				if (file.isDirectory() || file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}
	
	private static class XmlFileFilter extends FileFilter {
		@Override
		public String getDescription() {
			return "xml";
		}

		@Override
		public boolean accept(File file) {
			for (String extension : xmlFileExtensions) {
				if (file.isDirectory() || file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}
	
	private static class HaarModel{
		private String key;
		private int value;
		private HaarModel(String k, int v){
			key = k;
			value = v;
		}
		public String toString(){
			return key;
		}
	}
}
