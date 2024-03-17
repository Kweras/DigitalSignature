import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class Main {
    private static byte[] sigToVerify;

    public static void main(String[] args) {

        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("Digital Signature Signer");

        ImageIcon icon = new ImageIcon("img.png");
        frame.setIconImage(icon.getImage());

        JTabbedPane tabbedPane = new JTabbedPane();

        //First card

        JPanel keysCard = new JPanel();
        JButton gButton = new JButton("Generate keys");
        gButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                KeyPairGenerator keyGen = null;
                try {
                    keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
                    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
                    keyGen.initialize(1024, random);
                    KeyPair pair = keyGen.generateKeyPair();
                    PrivateKey priv = pair.getPrivate();
                    PublicKey pub = pair.getPublic();

                    byte[] pubkey = pub.getEncoded();
                    FileOutputStream pubout = new FileOutputStream("publickey.txt");
                    pubout.write(pubkey);
                    pubout.close();

                    byte[] prikey = priv.getEncoded();
                    FileOutputStream priout = new FileOutputStream("privatekey.txt");
                    priout.write(prikey);
                    priout.close();

                    JOptionPane.showMessageDialog(frame, "Keys generated successfully at program's directory", "Keys generated", JOptionPane.PLAIN_MESSAGE);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        });


        JPanel genPanel = new JPanel();
        JLabel welcomeText = new JLabel("<html>" +
                "<font size='72' color='gray' ><strong>Digital Signature Generator</strong></font>" +
                "</html>",JLabel.CENTER
);
        JLabel description = new JLabel("<html>" +
                "<font size='16px' color='gray' margin-top='100'><strong>Created by Kacper Nowak</strong></font>" +
                "</html>",JLabel.CENTER
        );
        JLabel message = new JLabel("<html>" +
                "<font-size='8px' color='gray' margin-top='100'><strong>Click below to generate your private and public keys</strong></font>" +
                "</html>",JLabel.CENTER
        );
        genPanel.setLayout(new GridLayout(4,1));
        keysCard.setLayout(new BorderLayout());

        genPanel.add(welcomeText);
        genPanel.add(description);
        genPanel.add(message);
        genPanel.add(gButton);

        genPanel.setPreferredSize(new Dimension(0,375));
        keysCard.add(genPanel,BorderLayout.SOUTH);

        //Second card
        final FileInputStream[] fts = {null};
        final Signature[] dsa = new Signature[1];

        JPanel signCard = new JPanel();
        JLabel prvLabel = new JLabel("no file selected");
        prvLabel.setBorder(new EmptyBorder(0,15,0,15));
        JLabel docLabel = new JLabel("no file selected");
        docLabel.setBorder(new EmptyBorder(0,15,0,15));

        JButton selectCertButton = new JButton("Select private key");
        selectCertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.showOpenDialog(null);
                prvLabel.setText("<HTML>Selected file: "+fileChooser.getSelectedFile().getAbsolutePath()+"</HTML>");

                try {
                    FileInputStream file = new FileInputStream(fileChooser.getSelectedFile().getAbsolutePath());
                    byte[] priv = new byte[file.available()];
                    file.read(priv);
                    file.close();
                    System.out.println(priv);

                    PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(priv);
                    System.out.println("1");
                    KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
                    System.out.println("2");
                    PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
                    System.out.println("3");

                    dsa[0] = Signature.getInstance("SHA1withDSA", "SUN");
                    dsa[0].initSign(privKey);
                }catch (InvalidKeySpecException ex){
                    JOptionPane.showMessageDialog(frame, "Select correct private key!", "Error!", JOptionPane.PLAIN_MESSAGE);
                    prvLabel.setText("no file selected");
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }


            }
        });

        JButton selectFileButton = new JButton("Select document to sign");

        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.showOpenDialog(null);
                docLabel.setText("<HTML>Selected file: "+fileChooser.getSelectedFile().getAbsolutePath()+"</HTML>");

                try {
                    fts[0] = new FileInputStream(fileChooser.getSelectedFile().getAbsolutePath());
                    BufferedInputStream bufin = new BufferedInputStream(fts[0]);
                    byte[] buffer = new byte[1024];
                    int len;
                    while (bufin.available() != 0)
                    {
                        len = bufin.read(buffer);
                        dsa[0].update(buffer, 0, len);
                    };
                    bufin.close();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        });



        JButton signButton = new JButton("Sign document!");
        signButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    byte[] realSig = dsa[0].sign();
                    FileOutputStream sigfos = new FileOutputStream("signature.txt");
                    sigfos.write(realSig);
                    sigfos.close();

                    JOptionPane.showMessageDialog(frame, "Document signed successfully!", "Signing status", JOptionPane.PLAIN_MESSAGE);
                    docLabel.setText("");
                    prvLabel.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Signing error! " + ex, "Signing status", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);
                }



            }


        });

        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        signCard.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300,300));
        leftPanel.setLayout(new GridLayout(3,1));
        rightPanel.setPreferredSize(new Dimension(300,300));
        rightPanel.setLayout(new GridLayout(3,1));
        leftPanel.setPreferredSize(new Dimension(300,300));
        bottomPanel.setLayout(new BorderLayout());

        rightPanel.add(prvLabel);
        rightPanel.add(docLabel);

        bottomPanel.add(signButton,BorderLayout.SOUTH);

        signButton.setSize(600,50);
        signCard.add(leftPanel, BorderLayout.WEST);
        signCard.add(rightPanel, BorderLayout.EAST);
        leftPanel.add(selectCertButton);
        leftPanel.add(selectFileButton);
        signButton.setPreferredSize(new Dimension(75,75));
        signCard.add(bottomPanel, BorderLayout.SOUTH);

        //Third card

        JLabel pLabel = new JLabel("no file selected");
        pLabel.setBorder(new EmptyBorder(0,15,0,15));
        JLabel sLabel = new JLabel("no file selected");
        sLabel.setBorder(new EmptyBorder(0,15,0,15));
        JLabel dLabel = new JLabel("no file selected");
        dLabel.setBorder(new EmptyBorder(0,15,0,15));

        try {
            final PublicKey[] pubKey = new PublicKey[1];
            final boolean[] check = {false};
            final Signature[] sig = {null};
            JButton selectPublicButton = new JButton("Select public key");
            selectPublicButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                    fileChooser.showOpenDialog(null);

                    File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                    System.out.println(file);
                    pLabel.setText("<HTML>Selected file: "+fileChooser.getSelectedFile().getAbsolutePath()+"</HTML>");

                    FileInputStream keyfis = null;

                    try {
                        keyfis = new FileInputStream(file);
                        byte[] encKey = new byte[keyfis.available()];
                        keyfis.read(encKey);
                        keyfis.close();

                        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
                        KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
                        pubKey[0] = keyFactory.generatePublic(pubKeySpec);
                        check[0] = true;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }


                }
            });

            JButton selectCertToVerifyButton = new JButton("Select signature");
            selectCertToVerifyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                    fileChooser.showOpenDialog(null);

                    File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                    sLabel.setText("<HTML>Selected file: "+fileChooser.getSelectedFile().getAbsolutePath()+"</HTML>");
                    FileInputStream sigfis = null;

                    try {
                        sigfis = new FileInputStream(file);
                        sigToVerify = new byte[sigfis.available()];
                        sigfis.read(sigToVerify);
                        sigfis.close();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }


                }
            });

            JPanel verifyCard = new JPanel();
            JButton selectSignedButton = new JButton("Select signed document");
            selectSignedButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                    fileChooser.showOpenDialog(null);


                    try {
                        File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                        dLabel.setText("<HTML>Selected file: "+fileChooser.getSelectedFile().getAbsolutePath()+"</HTML>");
                        sig[0] = Signature.getInstance("SHA1withDSA", "SUN");
                        System.out.println(sig[0]);
                        sig[0].initVerify(pubKey[0]);
                        System.out.println(sig[0]);
                        FileInputStream datafis;
                        datafis = new FileInputStream(file);
                        BufferedInputStream bufin = new BufferedInputStream(datafis);
                        byte[] buffer = new byte[1024];
                        int len;
                        while (bufin.available() != 0) {
                            len = bufin.read(buffer);
                            sig[0].update(buffer, 0, len);
                        }

                        bufin.close();
                        datafis = null;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }


                }


            });

            JButton verifyButton = new JButton("Verify document!");
            verifyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(sig[0]);
                    boolean verifies = false;

                    try {
                        verifies = sig[0].verify(sigToVerify);
                        System.out.println("signature verifies: " + verifies);

                        pLabel.setText("");
                        sLabel.setText("");
                        dLabel.setText("");
                        if (verifies){
                            JOptionPane.showMessageDialog(frame, "Verified successfully!", "Verification status", JOptionPane.PLAIN_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(frame, "File is not signed up!", "Verification status", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SignatureException ex) {
                        throw new RuntimeException(ex);
                    }


                }
            });

            JPanel lPanel = new JPanel();
            JPanel rPanel = new JPanel();
            JPanel bPanel = new JPanel();
            verifyCard.setLayout(new BorderLayout());
            lPanel.setPreferredSize(new Dimension(300,300));
            lPanel.setLayout(new GridLayout(3,1));
            rPanel.setPreferredSize(new Dimension(300,300));
            rPanel.setLayout(new GridLayout(3,1));
            lPanel.setPreferredSize(new Dimension(300,300));
            bPanel.setLayout(new BorderLayout());

            lPanel.add(selectPublicButton);
            lPanel.add(selectCertToVerifyButton);
            lPanel.add(selectSignedButton);

            rPanel.add(pLabel);
            rPanel.add(sLabel);
            rPanel.add(dLabel);

            bPanel.add(verifyButton);

            verifyCard.add(lPanel, BorderLayout.WEST);
            verifyCard.add(rPanel, BorderLayout.EAST);
            verifyCard.add(bPanel, BorderLayout.SOUTH);
            verifyButton.setPreferredSize(new Dimension(75,75));


            tabbedPane.addTab("Keys generator", keysCard);
            tabbedPane.addTab("Document signing", signCard);
            tabbedPane.addTab("Document verifying", verifyCard);

        } catch (Exception ex) {
            System.out.println("ERROR");
        }

        frame.add(tabbedPane);

        tabbedPane.setSize(600, 600);
        frame.setSize(600, 600);
        frame.pack();
        frame.setVisible(true);

    }
}
