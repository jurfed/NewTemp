package rxexample;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class RxExample {

    public static void main(String[] args) {
        TestService service = new TestService();
        UI ui = new UI(service);
    }
}

class UI extends JFrame {

    private final TestService service;

    UI(TestService service) {
        this.service = service;
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));
        JButton button = new JButton("Load");
        final JLabel label = new JLabel();
        final JLabel infoLabel = new JLabel();
        panel.add(button);
        panel.add(infoLabel);
        panel.add(label);
        add(panel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        button.addActionListener(e -> {
            infoLabel.setText("Loading...");
            this.service.getMessage()
                    .subscribeOn(Schedulers.io())
                    .subscribe(s -> label.setText(s),
                            throwable -> infoLabel.setText(throwable.getMessage()),
                            () -> infoLabel.setText("Complete!"));
        });
    }
}

class TestService{
    @SneakyThrows
    public Observable<String> getMessage() {
        List<String> list = Arrays.asList(new String[]{"One", "Two", "Three"});
        return Observable.fromIterable(list).map(s -> {
            throw new  RuntimeException("Error");
//            Thread.sleep(2000);
//            return s;
        });
    }
}
