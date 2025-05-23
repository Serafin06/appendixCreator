package pl.marko

import org.hibernate.cfg.Configuration
import pl.marko.bazaDanych.Budynek
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.*

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Test Hibernate + GUI")
        val panel = JPanel()
        val textField = JTextField(20)
        val button = JButton("Zapisz budynek")

        panel.add(textField)
        panel.add(button)

        frame.layout = BorderLayout()
        frame.add(panel, BorderLayout.CENTER)
        frame.setSize(400, 120)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true

        button.addActionListener { _: ActionEvent ->
            val adres = textField.text.trim()
            if (adres.isNotEmpty()) {
                val sessionFactory = Configuration()
                    .configure()
                    .buildSessionFactory()
                val session = sessionFactory.openSession()
                session.beginTransaction()
                session.persist(Budynek(adres = adres))
                session.transaction.commit()
                session.close()
                JOptionPane.showMessageDialog(frame, "Zapisano: $adres")
            }
        }
    }
}

