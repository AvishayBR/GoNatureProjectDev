package CommonClient.controllers;

import CommonUtils.MessagePopup;
import Entities.*;
import VisitorsControllers.ActiveOrdersPageController;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Date;

public class OrderBillPageController extends BaseController {
    @FXML
    private ImageView backBtn;

    @FXML
    private Pane billPane;

    @FXML
    private Text discountTxt;

    @FXML
    private Text fullPriceTxt;

    @FXML
    private Text dateTxt;

    @FXML
    private Text numVisitorsTxt;

    @FXML
    private Text orderIdTxt;

    @FXML
    private Text orderTypeTxt;

    @FXML
    private Text priceAfterDiscTxt;

    @FXML
    private Text signatureTxt;

    @FXML
    private MFXButton proceedBtn;

    @FXML
    private Text typeDescTxt;

    @FXML
    private Text priceTxt;

    private MessagePopup messageController;
    private boolean referredByEmployee;

    public void cleanup() {
        discountTxt.setText("");
        fullPriceTxt.setText("");
        numVisitorsTxt.setText("");
        orderIdTxt.setText("");
        orderTypeTxt.setText("");
        priceAfterDiscTxt.setText("");
        typeDescTxt.setText("");
    }

    public void setMessagePopup(MessagePopup messageController) {
        this.messageController = messageController;
    }

    public void closePopup() {
        messageController.closePopup(false);
    }

    public void proceedToPayment() {
        messageController.closePopup(true);
        if (referredByEmployee) {
            applicationWindowController.loadEmployeesPage("GenerateBillPage");
        } else {
            applicationWindowController.loadVistorsPage("ActiveOrdersPage");
            Object controller = applicationWindowController.getCurrentActiveController();
            if (controller instanceof ActiveOrdersPageController) {
                ((ActiveOrdersPageController) controller).start();
                ((ActiveOrdersPageController) controller).populateTable((ArrayList) (ClientCommunicator.msg.getMsgData()));
            }
        }
    }

    public void start(Order order, boolean referredPostOrder) {
        this.referredByEmployee = referredPostOrder;
        signatureTxt.setVisible(!referredPostOrder);


        Discount discountType = Discount.getDiscountType(order.getOrderType(), order.getOrderStatus());
        Double fullPrice = discountType !=
                Discount.PREPAID_PREORDERED_GROUP_DISCOUNT ?
                order.getNumOfVisitors() * Order.pricePerVisitor :
                (order.getNumOfVisitors() - 1) * Order.pricePerVisitor;
        fullPriceTxt.setText(String.format("%.2f", fullPrice));


        if (discountType != null) {
            discountTxt.setText(Discount.displayString(discountType));
            priceAfterDiscTxt.setText(String.format("%.2f", Discount.applyDiscount(fullPrice, discountType)));
        }

        Date date = new Date();
        dateTxt.setText(date.toString());
        orderIdTxt.setText(order.getOrderID());
        numVisitorsTxt.setText(String.valueOf(order.getNumOfVisitors()));
        priceTxt.setText(String.valueOf(Order.pricePerVisitor));
        orderTypeTxt.setText(order.getOrderType().toString());

        String orderTypeDescription = "";
        if (order.getOrderType() == OrderType.ORD_TYPE_SINGLE) {
            orderTypeTxt.setText("Single visitation");
            if (order.getOrderStatus() == OrderStatus.STATUS_SPONTANEOUS_ORDER) {
                orderTypeDescription = order.getNumOfVisitors() > 5 ? "Spontaneous family-sized visitation" : "Spontaneous single visitation";
            } else {
                orderTypeDescription = order.getNumOfVisitors() > 5 ? "Pre-ordered family-sized visitation" : "Pre-ordered single visitation";
            }
        } else if (order.getOrderType() == OrderType.ORD_TYPE_GROUP) {
            orderTypeTxt.setText("Group visitation");
            orderTypeDescription = order.getOrderStatus() == OrderStatus.STATUS_SPONTANEOUS_ORDER ? "Spontaneous group visitation" : "Pre-ordered group visitation";
        }

        typeDescTxt.setText(orderTypeDescription);
    }
}