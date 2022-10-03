/*-
 * Copyright (C) 2007 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.hfsexplorer.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.catacombae.hfs.Journal;
import org.catacombae.hfs.Journal.Transaction;
import org.catacombae.hfs.types.hfsplus.JournalHeader;
import org.catacombae.hfs.types.hfsplus.JournalInfoBlock;
import org.catacombae.util.Util;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class JournalInfoPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JPanel contentsPanel;
    private JournalInfoBlockPanel infoBlockPanel;
    private StructViewPanel journalHeaderPanel;
    private JComponent journalContentsPanel;
    private JPanel noJournalPanel;
    private JLabel noJournalLabel;
    private CardLayout layout;

    public JournalInfoPanel(Journal journal) {
        tabbedPane = new JTabbedPane();
	contentsPanel = new JPanel();
	infoBlockPanel = new JournalInfoBlockPanel();
        journalHeaderPanel = null;
        journalContentsPanel = null;
	noJournalPanel = new JPanel();
	noJournalLabel = new JLabel("No journal present", SwingConstants.CENTER);
	layout = new CardLayout();

	contentsPanel.setLayout(new BorderLayout());
	contentsPanel.add(infoBlockPanel, BorderLayout.CENTER);
        tabbedPane.insertTab("Info block", null, contentsPanel, "The journal " +
                "info block, describing the location of the journal.", 0);

	noJournalPanel.setLayout(new BorderLayout());
	noJournalPanel.add(noJournalLabel, BorderLayout.CENTER);

	setLayout(layout);
	add(noJournalPanel, "A");
        add(tabbedPane, "B");
	layout.show(this, "A");

	//pack();
        _setFields(journal);
    }

    private void _setFields(Journal journal) {
        JournalInfoBlock infoBlock = journal.getJournalInfoBlock();

        if(journalContentsPanel != null) {
            tabbedPane.remove(2);
        }

        if(journalHeaderPanel != null) {
            tabbedPane.remove(1);
        }

        infoBlockPanel.setFields(infoBlock);

        final JournalHeader journalHeader = journal.getJournalHeader();
        if(journalHeader == null) {
            if(infoBlock.getFlagJournalNeedInit()) {
                noJournalLabel.setText("Journal not initialized.");
            }
            else if(infoBlock.getFlagJournalOnOtherDevice()) {
                noJournalLabel.setText("Journal is located on other device " +
                        "(0x" +
                        Util.toHexStringBE(infoBlock.getDeviceSignature()) +
                        ").");
            }
            else if(!infoBlock.getFlagJournalInFS()) {
                noJournalLabel.setText("Journal is not located inside the " +
                        "filesystem (but where?).");
            }
            else {
                noJournalLabel.setText("Unknown error while loading the " +
                        "journal header.\n");
            }

            layout.show(this, "A");
            return;
        }

        journalHeaderPanel =
                new StructViewPanel("Journal header",
                (journalHeader.isLittleEndian() ? "Little" : "Big") +
                "-endian fields", journalHeader.getStructElements());
        tabbedPane.insertTab("Header", null, journalHeaderPanel, "The " +
                "journal header, describing the data inside the journal.", 1);

        try {
            Transaction[] transactions = journal.getPendingTransactions();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, false, "UTF-8");

            for(int i = 0; i < transactions.length; ++i) {
                final Transaction t = transactions[i];
                ps.println("Transaction " + i + ":");
                for(int j = 0; j < t.blockLists.length; ++j) {
                    ps.println(" blockLists[" + j + "]:");
                    t.blockLists[j].print(ps, "  ");
                }
            }

            ps.flush();
            ps.close();

            JTextArea transactionsTextArea =
                    new JTextArea(new String(baos.toByteArray(), "UTF-8"));

            baos.close();

            transactionsTextArea.setEditable(false);
            journalContentsPanel = new JScrollPane(transactionsTextArea);
            tabbedPane.insertTab("Transactions", null, journalContentsPanel,
                    "The journal's pending transactions.", 2);
        } catch(Exception e) {
            e.printStackTrace();
        }

	layout.show(this, "B");
    }

    public void setFields(Journal journal) {
        _setFields(journal);
    }
}
