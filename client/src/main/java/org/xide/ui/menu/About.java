package org.xide.ui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Scrollable;

import org.xide.config.CodeExamples;
import org.xide.ui.Container;

import io.simpleide.api.Editors;
import io.simpleide.ui.editor.Editor;
import io.simpleide.ui.treefile.TypeFile;

/**
 * "About" tab panel of the exam environment: welcome text, supported languages, judging
 * explanation, exam integrity rules, bundled example codes per language, and developer
 * credits.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class About extends JPanel implements Scrollable {

	private static final Color WELCOME_COLOR = new Color(255, 241, 242);

	private static final Color TEXT_COLOR = new Color(34, 41, 52);

	private static final Color MUTED_COLOR = new Color(103, 113, 128);

	private static final Color INTEGRITY_COLOR = new Color(255, 225, 229);

	private static final String WELCOME_TEXT = """
			X-IDE is a BOCA-inspired environment for competitive programming exams. Read problem statements, write solutions, and follow your results — all in one place.

			## What is BOCA?
			BOCA (BOCA Online Contest Administrator) is a programming contest management system created for the SBC Programming Contest and used in Brazilian ICPC events. It manages teams, problems, submissions, judging, and the scoreboard. X-IDE takes inspiration from this workflow.

			## How to use this environment
			• Editor — write, compile, and test your solution, then submit it.
			• Problems — read problem statements and check your submission status.
			• About — find information about the exam environment.

			The interface is designed to help you focus on solving problems. When you finish, select **End session** to close your exam session.
			""";

	private static final long serialVersionUID = 1L;

	public About() {
		super(new GridBagLayout());
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEmptyBorder());

		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(Color.WHITE);
		content.setMaximumSize(new Dimension(1100, Integer.MAX_VALUE));
		addContentRow(content, new Container(WELCOME_TEXT, WELCOME_COLOR, TEXT_COLOR), 0);
		addContentRow(content, section("Supported languages", createLanguageCards()), 1);
		addContentRow(content, section("How judging works", createJudgingCards()), 2);
		addContentRow(content, section("Exam rules", createIntegrityNotice()), 3);
		addContentRow(content, section("Example codes", helloWordInLinguages()), 4);
		addContentRow(content, section("Developers", createDeveloperCards()), 5);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		add(content, constraints);
	}

	private void addContentRow(JPanel content, Component component, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.weightx = 1.0;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		content.add(component, constraints);
	}

	private JPanel createLanguageCards() {
		String[][] languages = { { "Java", ".java · .jav" }, { "Kotlin", ".kt · .kts" },
				{ "Groovy", ".groovy · .gvy · .gy · .gsh" }, { "JavaScript", ".js · .jsx · .mjs · .cjs · .jscript" },
				{ "Python 3", ".py · .pyi · .pyw · .py3" }, { "C#", ".cs · .csx" },
				{ "C++", ".cpp · .cc · .cxx · .hxx" }, { "C", ".c" }, { "Go", ".go" }, { "Rust", ".rs" } };
		JPanel cards = responsiveGrid();
		for (String[] language : languages) {
			cards.add(card(language[0], language[1]));
		}
		return cards;
	}

	private JPanel helloWordInLinguages() {
		JPanel hellowords = responsiveGrid();
		addLinguage(hellowords, "Python", CodeExamples.PYTHON, TypeFile.PYTHON);
		addLinguage(hellowords, "Java", CodeExamples.JAVA, TypeFile.JAVA);
		addLinguage(hellowords, "Kotlin", CodeExamples.KOTLIN, TypeFile.KOTLIN);
		addLinguage(hellowords, "Groovy", CodeExamples.GROOVY, TypeFile.GROOVY);
		addLinguage(hellowords, "C", CodeExamples.C, TypeFile.C);
		addLinguage(hellowords, "C++", CodeExamples.CPP, TypeFile.CPP);
		addLinguage(hellowords, "C#", CodeExamples.CSHARP, TypeFile.CSHARP);
		addLinguage(hellowords, "Go", CodeExamples.GO, TypeFile.GO);
		addLinguage(hellowords, "Rust", CodeExamples.RUST, TypeFile.RUST);
		return hellowords;
	}

	private void addLinguage(JPanel parent, String name, String code, TypeFile typeFile) {
		JPanel container = new JPanel(new BorderLayout(0, 5));
		container.setBackground(Color.WHITE);
		container.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 231, 238)),
				BorderFactory.createEmptyBorder(6, 6, 6, 6)));

		JLabel title = new JLabel(name);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
		title.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

		Editor editor = Editors.create(code);
		editor.setEditable(false);
		editor.setSyntaxStyle(typeFile);
		var codePane = Editors.scrollPane(editor);
		codePane.setPreferredSize(new Dimension(320, 132));
		codePane.setMinimumSize(new Dimension(0, 110));

		container.add(title, BorderLayout.NORTH);
		container.add(codePane, BorderLayout.CENTER);

		parent.add(container);
	}

	private JPanel createDeveloperCards() {
		JPanel cards = responsiveGrid();
		cards.add(card("Bruno R.N.S", "GitHub: @BrunoRNS\nContact: brunorns05@outlook.com"));
		cards.add(card("Luis Otávio", "GitHub: @LuisOtavio13 · @luisotavio15\nContact: roberdoogarcia@gmail.com"));
		return cards;
	}

	private JPanel createJudgingCards() {
		String[][] explanations = {
				{ "Subtasks", "A problem may be split into subtasks, each covering a group of test cases. "
						+ "Your score depends on which subtasks your solution passes; a subtask is passed only when its tests pass." },
				{ "Execution time", "Your program must finish within the time limit stated for the exercise "
						+ "and your programming language. Exceeding that limit results in Time Limit Exceeded (TLE)." },
				{ "Code review", "After all subtasks pass, an automated review may check whether your solution follows "
						+ "the recommended approach. It may issue a warning, but that warning does not change a correct result." },
				{ "Accepted (AC)", "The solution produced the expected answers and passed all required tests." },
				{ "Balloon (if enabled)", "A balloon marks an exercise solved: your submission was accepted. In some "
						+ "competitions, it also represents the celebratory balloon awarded to the team. Its use depends on the event." },
				{ "Wrong Answer (WA)", "The program ran, but at least one answer did not match the expected result." },
				{ "Presentation Error (PE)",
						"The answer was presented in a format the checker did not accept, such as "
								+ "incorrect spacing, line breaks, or other output formatting." },
				{ "Time Limit Exceeded (TLE)", "The program took longer than the exercise allows to finish." },
				{ "Memory Limit Exceeded (MLE)", "The program used more memory than the exercise allows." } };
		JPanel cards = responsiveGrid();
		for (String[] explanation : explanations) {
			cards.add(card(explanation[0], explanation[1]));
		}
		return cards;
	}

	private JPanel createIntegrityNotice() {
		return new Container(
				"""
						## Academic integrity
						The use of **AI tools is strictly prohibited** during the exam. All submitted work must be your own. Plagiarism, copied code, or unauthorized collaboration will result in immediate disqualification from the exam.
						""",
				INTEGRITY_COLOR, new Color(127, 29, 29));
	}

	private JPanel responsiveGrid() {
		JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
		grid.setOpaque(false);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);

		grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		grid.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

		grid.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				int columns = grid.getWidth() < 540 ? 1 : 2;

				GridLayout layout = (GridLayout) grid.getLayout();

				if (layout.getColumns() != columns) {
					layout.setColumns(columns);
					grid.revalidate();
				}
			}
		});

		return grid;
	}

	private JPanel section(String title, JPanel body) {
		JPanel section = new JPanel(new BorderLayout(0, 12));
		section.setOpaque(false);

		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		section.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

		JLabel heading = new JLabel(title);
		heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		heading.setForeground(TEXT_COLOR);

		section.add(heading, BorderLayout.NORTH);
		section.add(body, BorderLayout.CENTER);

		return section;
	}

	private JPanel card(String title, String details) {
		JPanel card = new JPanel(new BorderLayout(0, 6));
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 231, 238)),
				BorderFactory.createEmptyBorder(12, 14, 12, 14)));
		JLabel name = new JLabel(title);
		name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		name.setForeground(TEXT_COLOR);
		JTextArea description = new JTextArea(details);
		description.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		description.setForeground(MUTED_COLOR);
		description.setOpaque(false);
		description.setEditable(false);
		description.setFocusable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		card.add(name, BorderLayout.NORTH);
		card.add(description, BorderLayout.CENTER);
		card.setMinimumSize(new Dimension(0, 64));
		return card;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return Math.max(16, visibleRect.height - 16);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

}
