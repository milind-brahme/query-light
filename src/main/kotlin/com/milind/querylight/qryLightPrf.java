package com.milind.querylight;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import say.swing.JFontChooser;

import java.awt.*;
import java.util.prefs.Preferences;

/**
 *
 * @author hv655
 */
public class qryLightPrf extends javax.swing.JPanel {

    /**
     * Creates new form qryLightPrf
     */
    public qryLightPrf() {

        initComponents();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnSetGridFont = new javax.swing.JButton();
        txtNoOfRecords = new javax.swing.JTextField();
        btnSeteditorFont = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Set Grid Font");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 24, 132, -1));

        jLabel2.setText("No of Records to fetch on query");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 88, -1, -1));

        btnSetGridFont.setText("..");
        btnSetGridFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetGridFontActionPerformed(evt);
            }
        });

        txtNoOfRecords.setText("100");

        btnSeteditorFont.setText("..");
        btnSeteditorFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSeteditorFontActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSetGridFont, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtNoOfRecords)
                    .addComponent(btnSeteditorFont))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSetGridFont, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSeteditorFont, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNoOfRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnSetGridFont, btnSeteditorFont});

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(192, 11, -1, -1));

        jLabel3.setText("Set Editor Font");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 56, 132, -1));

        jLabel4.setText("     Close and reopen Editor / Connection to have these settings take effect.");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 286, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnSetGridFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetGridFontActionPerformed
   JFontChooser fontChooser = new JFontChooser();
   int result = fontChooser.showDialog(this);
   if (result == JFontChooser.OK_OPTION)
   {
       
        Font font = fontChooser.getSelectedFont(); 
        //System.out.println("Selected Font : " + font); 
        String fontName = font.getFontName();
        int fontstyle = font.getStyle();
        int fontsize = font.getSize();
        Preferences prf = Preferences.userNodeForPackage(StmtRunScreen1.class);
        prf.put("fontName", fontName);
        prf.putInt("fontstyle", fontstyle);
        prf.putInt("fontsize", fontsize);
  }
  
        
        
    }//GEN-LAST:event_btnSetGridFontActionPerformed

    private void btnSeteditorFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSeteditorFontActionPerformed
        JFontChooser fontChooser = new JFontChooser();
   int result = fontChooser.showDialog(this);
   if (result == JFontChooser.OK_OPTION)
   {
       
        Font font = fontChooser.getSelectedFont(); 
        //System.out.println("Selected Font : " + font); 
        String fontName = font.getFontName();
        int fontstyle = font.getStyle();
        int fontsize = font.getSize();
        Preferences prf = Preferences.userNodeForPackage(StmtRunScreen1.class);
        prf.put("EditorfontName", fontName);
        prf.putInt("Editorfontstyle", fontstyle);
        prf.putInt("Editorfontsize", fontsize);
     }
   
    }//GEN-LAST:event_btnSeteditorFontActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSetGridFont;
    private javax.swing.JButton btnSeteditorFont;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtNoOfRecords;
    // End of variables declaration//GEN-END:variables
}
