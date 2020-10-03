package alararestaurant.service;

import alararestaurant.domain.dtos.xml.ItemDto;
import alararestaurant.domain.dtos.xml.OrderDto;
import alararestaurant.domain.dtos.xml.OrderRootDto;
import alararestaurant.domain.entities.Employee;
import alararestaurant.domain.entities.Order;
import alararestaurant.domain.entities.OrderItem;
import alararestaurant.repository.*;
import alararestaurant.util.FileUtil;
import alararestaurant.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    private final static String ORDERS_XML_PATH = "E:\\Tai Lopez\\AlaraRestaurant-skeleton\\AlaraRestaurant\\src\\main\\resources\\files\\orders.xml";

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, EmployeeRepository employeeRepository, ItemRepository itemRepository, OrderItemRepository orderItemRepository, FileUtil fileUtil, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
        this.itemRepository = itemRepository;
        this.orderItemRepository = orderItemRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean ordersAreImported() {
        return this.orderRepository.count() > 0;
    }

    @Override
    public String readOrdersXmlFile() throws IOException {
        return this.fileUtil.readFile(ORDERS_XML_PATH);
    }

    @Override
    public String importOrders() throws JAXBException {
        StringBuilder sb = new StringBuilder();

        JAXBContext context = JAXBContext.newInstance(OrderRootDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        OrderRootDto orders = (OrderRootDto) unmarshaller.unmarshal(new File(ORDERS_XML_PATH));

        orderLoop:
        for (OrderDto orderDto : orders.getOrders()) {
            Order order = this.modelMapper.map(orderDto, Order.class);
            Employee employee = this.employeeRepository.findByName(orderDto.getEmployee());

            if (!this.validationUtil.isValid(order) || employee == null) {
                sb.append("Invalid data format.").append(System.lineSeparator());
                continue;
            }
            order.setEmployee(employee);

            List<OrderItem> itemList = new ArrayList<>();
            for (ItemDto item : orderDto.getItems().getItems()) {
                if (this.itemRepository.findByName(item.getName()) == null) {
                    continue orderLoop;
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setItem(this.itemRepository.findByName(item.getName()));
                orderItem.setQuantity(item.getQuantity());
                itemList.add(orderItem);

                this.orderItemRepository.saveAndFlush(orderItem);
            }

            order.setOrderItems(itemList);
            this.orderRepository.saveAndFlush(order);
            sb.append(String.format("Order for %s on %s added%n", orderDto.getCustomer(), orderDto.getDateTime()));
        }

        return sb.toString().trim();
    }

    @Override
    public String exportOrdersFinishedByTheBurgerFlippers() {
        StringBuilder sb = new StringBuilder();

        List<Order> orders = this.orderRepository.finishedByBuegerFlipper();
        for (Order order : orders) {
            sb.append(String.format("Name: %s%nOrders:%n    Customer: %s%n    Items:%n",
                    order.getEmployee().getName(), order.getCustomer()));
            for (OrderItem orderItem : order.getOrderItems()) {
                sb.append(String.format("    Name: %s%n    Price: %s%n    Quantity: %d%n%n",
                        orderItem.getItem().getName(),
                        orderItem.getItem().getPrice(),
                        orderItem.getQuantity()));
            }
        }

        return sb.toString().trim();
    }
}
