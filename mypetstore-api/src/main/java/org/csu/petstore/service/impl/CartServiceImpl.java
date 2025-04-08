package org.csu.petstore.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.csu.petstore.common.CommonResponse;
import org.csu.petstore.entity.Cart;
import org.csu.petstore.persistence.CartMapper;
import org.csu.petstore.persistence.ItemMapper;
import org.csu.petstore.persistence.ItemQuantityMapper;
import org.csu.petstore.service.CartService;
import org.csu.petstore.service.CatalogService;
import org.csu.petstore.vo.CartItem;
import org.csu.petstore.vo.CartItemListMapper;
import org.csu.petstore.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("cartService")
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemQuantityMapper itemQuantityMapper;

    @Autowired
    private CatalogService catalogService;



    @Override
    public List<Cart> getCartListByUserId(String userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        return cartMapper.selectList(queryWrapper);
    }

    @Override
    public CommonResponse<List<CartItem>> getCartItemListByUserId(String userId) {
        List<CartItem> cartItemList = new ArrayList<>();

        QueryWrapper<Cart> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("userId", userId);
        List<Cart> cartList = cartMapper.selectList(queryWrapper1);
        for (Cart cart : cartList) {

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            ItemVO itemVO = catalogService.getItem(cart.getItemId()).getData();
            cartItem.setItemVO(itemVO);
            cartItem.setInStock(itemQuantityMapper.selectById(cart.getItemId()).getQuantity());
            cartItem.setTotal((itemVO.getListPrice().multiply(new BigDecimal(cart.getQuantity()))));
            cartItemList.add(cartItem);
        }
        if(cartItemList.size() == 0){
            return CommonResponse.createForSuccessMessage("购物车内没有商品");
        }
        return CommonResponse.createForSuccess("购物车内商品列表获取成功",cartItemList);
    }


    @Override
    public void addItemToCart(String username, String itemId) {
        Cart cart = new Cart();
        cart.setUserid(username);
        cart.setItemId(itemId);
        cart.setQuantity(1);
        List<Cart> cartList = getCartListByUserId(username);
        List<String> itemIdList = new ArrayList<>();
        for (Cart cartItem : cartList) {
            itemIdList.add(cartItem.getItemId());}
        if (itemIdList.contains(itemId)) {
            cart.setQuantity(cartList.get(itemIdList.indexOf(itemId)).getQuantity()+1);
            cartMapper.updateCart(cart.getUserid(), cart.getItemId(), cart.getQuantity());
        }
        else{
            cartMapper.insert(cart);
        }
        CommonResponse.createForSuccessMessage("商品项" + itemId + "加入购物车成功");

    }

    @Override
    public void updateCart(String username, String itemId, int quantity) {
        cartMapper.updateCart(username, itemId, quantity);
        CommonResponse.createForSuccessMessage("商品项" + itemId + "数量已更新为" + quantity);
    }

    @Override
    public void removeItem(String username, String itemId) {
        cartMapper.deleteCart(username, itemId);
        CommonResponse.createForSuccessMessage("商品项" + itemId + "已从购物车中删除");
    }

    @Override
    public void clearCart(String username) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", username);
        cartMapper.delete(queryWrapper);
        CommonResponse.createForSuccessMessage("购物车已清空");
    }

    @Override
    public void updateCartByCartItemList(List<CartItem> cartItemList, String userid) {
        for (CartItem cartItem : cartItemList) {
            Cart cart = cartItem.getCart();
            int quantity = cartItem.getCart().getQuantity();
            if (quantity < 1){
                removeItem(cart.getUserid(),cart.getItemId());
            }else{
                updateCart(cart.getUserid(), cart.getItemId(), quantity);
            }
        }
        CommonResponse.createForSuccessMessage("购物车已更新");
    }
}
