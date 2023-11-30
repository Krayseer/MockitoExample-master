package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

/**
 * Тестирование {@link ShoppingService}
 */
@ExtendWith(MockitoExtension.class)
class ShoppingServiceTest {

    @Mock
    public ProductDao productDao;

    @InjectMocks
    private ShoppingServiceImpl shoppingService;

    /**
     * Тестирование получения тележки для покупателя
     */
    @Test
    void testGetCart() {
        Customer customer = new Customer(12, "152");
        Assertions.assertNotNull(shoppingService.getCart(customer));
    }

    /**
     * Тестирование метода не нужно так как берутся все продукты из БД, изменения состояния, которое нужно проверить
     * не происходит
     */
    @Test
    void testGetAllProducts() {
    }

    /**
     * Тестирование метода не нужно, так как из БД берётся значение по имени товара, не идет изменение состояния,
     * которое нужно проверять
     */
    @Test
    void testGetProductByName() {
    }

    /**
     * Тестирование покупки товаров из пустой корзины
     * @throws BuyException ошибка покупки
     */
    @Test
    void testBuyProductsFromEmptyCart() throws BuyException {
        Customer customer = new Customer(1L, "customer");
        Cart cart = new Cart(customer);
        boolean result = shoppingService.buy(cart);
        Assertions.assertFalse(result);
    }

    /**
     * Тестирование покупки продуктов<br/>
     * Пользователю добавляются продукты в карзину, после чего идёт покупка, и нужно проверить, что изменилось
     * состояние продуктов (их количество)
     * @throws BuyException ошибка покупки
     */
    @Test
    void testBuyProducts() throws BuyException {
        Product testProduct = new Product();
        testProduct.setName("apple");
        testProduct.addCount(2);

        Product secondTestProduct = new Product();
        secondTestProduct.setName("banana");
        secondTestProduct.addCount(2);

        Customer customer = new Customer(1L, "testCustomer");
        Cart cart = new Cart(customer);
        cart.add(testProduct, 1);
        cart.add(secondTestProduct, 1);

        boolean result = shoppingService.buy(cart);

        Mockito.verify(productDao, Mockito.times(1)).save(testProduct);
        Mockito.verify(productDao, Mockito.times(1)).save(secondTestProduct);
        Assertions.assertTrue(result);
        Assertions.assertEquals(1, testProduct.getCount());
        Assertions.assertEquals(1, secondTestProduct.getCount());
    }

    /**
     * Тестирование покупки товаров, когда в ассортименте нет необходимого количества
     * @throws BuyException ошибка покупки
     */
    @Test
    void testBuyWhenNotExistValidProductsCount() throws BuyException {
        Product testProduct = new Product();
        testProduct.setName("apple");
        testProduct.addCount(5);

        Customer firstCustomer = new Customer(1L, "firstCustomer");
        Cart firstCustomerCart = new Cart(firstCustomer);
        firstCustomerCart.add(testProduct, 4);

        Customer secondCustomer = new Customer(2L, "secondCustomer");
        Cart secondCustomerCart = new Cart(secondCustomer);
        secondCustomerCart.add(testProduct, 2);

        boolean firstCustomerBuy = shoppingService.buy(firstCustomerCart);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            boolean secondCustomerBuy = shoppingService.buy(secondCustomerCart);
            Assertions.assertFalse(secondCustomerBuy);
        });
        Assertions.assertTrue(firstCustomerBuy);
        Assertions.assertEquals(
                String.format("В наличии нет необходимого количества товара %s", testProduct.getName()), exception.getMessage());
    }

}