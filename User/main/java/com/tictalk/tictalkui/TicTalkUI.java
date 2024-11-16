/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.tictalk.tictalkui;

/**
 *
 * @author viett
 */
public class TicTalkUI {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CreateGroupChat().setVisible(true);
            }
        });
    }
}
