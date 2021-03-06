package gui;

import pop3.Mail;
import pop3.MyPOP3;
import pop3.POP3Exception;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ReceiveFrame {
    private JFrame frame;
    private JPanel panelReceive;
    private JTable tableMail;
    private JScrollPane panelScroll;
    private JButton button_lastPage;
    private JButton button_nextPage;
    private JButton button_relush;
    private JButton button_delete;
    private JButton button_return;
    private JLabel label_page;

    private MyPOP3 pop3;
    private Mail[] currentPageMails;
    private int mailCount;
    private int currentPage;
    private int pageLastMail;
    private int pageFirstMail;
    private DefaultTableModel model;
    private int deleteCount =0;

    ReceiveFrame(MyPOP3 p) {
        pop3 = p;
        currentPage = 1;

        label_page.setFont(new java.awt.Font("Dialog", Font.BOLD | Font.ITALIC,   35));
        label_page.setForeground(Color.white);

        try {
            mailCount = pop3.mailCount();
        } catch (POP3Exception e) {
            String message = e.getMessage();
            JOptionPane.showMessageDialog(
                    frame,
                    message,
                    "获取邮件失败",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        button_nextPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(pageLastMail == mailCount){
                    JOptionPane.showMessageDialog(
                            frame,
                            "已经是最后一页啦",
                            "oops",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                pageFirstMail = pageLastMail+1;
                if(pageLastMail+10>=mailCount){
                    pageLastMail = mailCount;
                }else {
                    pageLastMail += 10;
                }
                currentPage++;
                initTable(pageFirstMail,pageLastMail);

            }
        });
        button_lastPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(currentPage == 1){
                    JOptionPane.showMessageDialog(
                            frame,
                            "已经是第一页啦",
                            "oops",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                if(pageFirstMail<=10){
                    pageFirstMail =1;
                    pageLastMail = 10;
                }else{
                    pageLastMail = pageFirstMail-1;
                    pageFirstMail = pageLastMail-9;
                }
                currentPage--;
                initTable(pageFirstMail,pageLastMail);

            }
        });

        tableMail.setRowHeight(50);
        tableMail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // 双击事件
                if (e.getClickCount() == 2) {
                    int row = tableMail.getSelectedRow();
                    jumpToMail(row);
                }
            }
        });
        button_return.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                frame.setVisible(false);
            }
        });
        button_delete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int[] rows = tableMail.getSelectedRows();


                Object[] options = {"确定","取消"};
                int response=JOptionPane.showOptionDialog(frame, "是否删除选中邮件？", "是否删除", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if(response==0){
                    try {
                        int[] newRows  = new int[rows.length];
                        for(int i=0; i< rows.length;i++){
                            newRows[i]= rows[i]+pageFirstMail;
                            System.out.println(newRows[i]);
                        }
                        pop3.deleteMails(newRows);
                        for (int row : rows) {
                            model.removeRow(row);
                            mailCount--;
                            pageLastMail--;
                        }
                    } catch (POP3Exception e1) {
                        String m = e1.getMessage();
                        JOptionPane.showMessageDialog(
                                frame,
                                m,
                                "删除失败",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                } else if(response==1) {
                   return;
                }
            }
        });
        button_relush.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    mailCount = pop3.mailCount();
                } catch (POP3Exception e1) {
                    String m = e1.getMessage();
                    JOptionPane.showMessageDialog(
                            frame,
                            m,
                            "刷新失败",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
                if(1<= mailCount&& mailCount <10){
                    initTable(1,mailCount);
                    pageLastMail = mailCount;
                } else if (mailCount >= 10) {
                    initTable(1,10);
                    pageLastMail = 10;
                }
                pageFirstMail =1;
                currentPage = 1;
                label_page.setText(Integer.toString(currentPage));
            }
        });
    }

    private void jumpToMail(int row) {
        System.out.println("JUMP TO:" + row);
        // TODO : 加载第row行的邮件信息，并进行跳转
        if(currentPageMails ==null){
            return;
        }
        Mail selectedMail  = currentPageMails[row];

        frame.setVisible(false);
        MailFrame mailFrame = new MailFrame(selectedMail);
        mailFrame.show(frame);
    }

    public void show() {
        frame = new JFrame("接收");
        frame.setContentPane(this.panelReceive);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        if(1<= mailCount&& mailCount <10){
            initTable(1,mailCount);
            pageLastMail = mailCount;
        } else if (mailCount >= 10) {
            initTable(1,10);
            pageLastMail = 10;
        }
        pageFirstMail =1;
    }

    private Object[][] getMailInfo(int start, int end) {
        // TODO : 在此获取所有邮件信息
        Object[][] result = new Object[end - start+1][3];
        try {
            currentPageMails = pop3.getMails(start, end);
            for(int i =0; i<= end -start; i++){
                result[i][0] = currentPageMails[i].getDate();
                result[i][1] = currentPageMails[i].getFromEmail();
                result[i][2] = currentPageMails[i].getContent();
            }

        } catch (POP3Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void initTable(int s, int e) {
        // 设置表格
        Object[] column = {"时间", "来自", "内容"};
        Object[][] data = getMailInfo(s, e);
        model = new DefaultTableModel(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMail.setModel(model);
        label_page.setText(Integer.toString(currentPage));
    }

}
