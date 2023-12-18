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
     * Тестирование получения тележки для покупателя.
     * Возможно стоит проверить что тележка не null, но не факт, так как при вызове метода просто создается новая
     * тележка.
     */
    @Test
    void testGetCart() {
        Customer customer = new Customer(12, "152");
        Cart customerCarts = shoppingService.getCart(customer);
        Assertions.assertNotNull(customerCarts);
    }

    /**
     * Тестирование метода не нужно, так как берутся все продукты из БД.
     * Изменения состояния, которое нужно проверить не происходит.
     */
    @Test
    void testGetAllProducts() {
    }

    /**
     * Тестирование метода не нужно, так как из БД берётся значение по имени товара.
     * Изменения состояния, которое нужно проверить не происходит.
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
        boolean shopResult = shoppingService.buy(cart);
        Assertions.assertFalse(shopResult);
    }

    /**
     * Тестирование невозможности покупки товара из-за его нехватки.<br/>
     * Были выявлены следующие (скорее) недочеты
     * <ol>
     *     <li>
     *         Адекватной проверки скорее всего не получится, потому что по факту сейчас метод {@link Cart#add(Product, int)}
     *         реализован некорректно, потому что сейчас количество продуктов уменьшается только при покупке товара, а не при
     *         вкладывании его в корзину, поэтому приходится сначала производить покупку у первого покупателя, потом у второго.
     *      </li>
     *      <li>
     *          Если продукт есть в n количестве, и мы захотим положить в корзину n штук, выкинется ошибка (странно).
     *          Проверка должна быть в сервисе должна быть <= а не <
     *      </li>
     * </ol>
     */
    @Test
    public void testBuyProductsException() throws BuyException {
        Product testProduct = new Product();
        testProduct.setName("apple");
        testProduct.addCount(5);

        Customer firstCustomer = new Customer(1L, "firstCustomer");
        Cart firstCustomerCart = new Cart(firstCustomer);
        firstCustomerCart.add(testProduct, 4);

        Customer secondCustomer = new Customer(2L, "secondCustomer");
        Cart secondCustomerCart = new Cart(secondCustomer);
        secondCustomerCart.add(testProduct, 4);

        shoppingService.buy(firstCustomerCart);

        BuyException buyException = Assertions.assertThrows(BuyException.class, () -> {
            boolean secondCustomerBuy = shoppingService.buy(secondCustomerCart);
            Assertions.assertFalse(secondCustomerBuy);
        });
        Assertions.assertEquals("В наличии нет необходимого количества товара apple", buyException.getMessage());
    }

    /**
     * Тестирование покупки продуктов<br/>
     * Пользователю добавляются продукты в корзину, после чего идёт покупка, и нужно проверить, что изменилось
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

}