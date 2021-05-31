package com.mbook.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mbook.entity.Account;
import com.mbook.entity.Author;
import com.mbook.entity.Cart;
import com.mbook.entity.CartDTO;
import com.mbook.entity.Product;
import com.mbook.repository.AccountRepository;
import com.mbook.repository.AuthorRepository;
import com.mbook.repository.CartRepository;
import com.mbook.repository.CartServiceInterface;
import com.mbook.repository.ProductRepository;


@Service
public class CartService implements CartServiceInterface {
	@Autowired
	CartRepository repo;
	@Autowired
	ProductRepository productRepo;
	@Autowired
	AccountRepository accountRepo;
	@Autowired
	AuthorRepository AuthorRepo;
	@Override
	public List<Cart> ListAll() {
		return repo.findAll();
	}
	@Override
	public void save(CartDTO cartDTO) {
		List<Author> listAu = AuthorRepo.findAll();
		List<Product> listPro = productRepo.findAll();
		List<Account> listAcc = accountRepo.findAll();
		UUID idtemp = UUID.fromString(cartDTO.getIdProduct());
		Product p = productRepo.findById(idtemp).get();
		Cart cartEntity = new Cart();
		List<Cart> listCart = repo.findAll();
		boolean checkCart = false;
		boolean checkOut = false;
		int checkProduct = -1; 
		for (Cart cart : listCart) {
			//Kiểm tra sản phẩm có nằm trong giỏ hàng của account
			if(cart.getAccountCart().getUsername().equalsIgnoreCase(cartDTO.getCreatedby())){
				checkCart = true;
				cartEntity = cart;
				//Product isExist
				checkProduct = cart.getListProduct().indexOf(p);
				if(cart.isCheckout() == true) {
					checkOut = true;
				}else {
					checkOut = false;
				}
			}
		}
		System.out.println("" );
		System.out.println("index Product: " + checkProduct);
		System.out.println("status cart: " + checkCart);
		//Handle Cart with status
		if(checkCart == true) {
			if(checkOut == true) {
				cartEntity = new Cart();
				Product productEntity = p;
				productEntity.setQuantity(1);
				Account accountEntity = accountRepo.findOneByUsername(cartDTO.getCreatedby());
				cartEntity.setCreatedby(cartDTO.getCreatedby());
				cartEntity.setAccountCart(accountEntity);
				cartEntity.getListProduct().add(productEntity);
				cartEntity.setQuantity(1);
				long total= 0;
				if(productEntity.getPricePresent() != null) {
					total += productEntity.getPricePresent() ;
				}else {
					total += productEntity.getPriceOld();
				}
				cartEntity.setTotalPrice(total);
			}else {
				Product productEntity = p;
				if(checkProduct != -1) {
					cartEntity.getListProduct().get(checkProduct).setQuantity(
							cartEntity.getListProduct().get(checkProduct).getQuantity()+ 1);
				}else {
					productEntity.setQuantity(1);
					cartEntity.getListProduct().add(productEntity);
				}
				long total= 0;
				for (Product product : cartEntity.getListProduct()) {
					if(product.getPricePresent() != null) {
						total += product.getPricePresent() *  product.getQuantity();
					}else {
						total += product.getPriceOld() *  product.getQuantity();
					}
				}
				cartEntity.setTotalPrice(total);
				cartEntity.setQuantity(cartEntity.getQuantity() + 1);
			}
			
		}else {
			Product productEntity =productRepo.findById(idtemp).get();
			productEntity.setQuantity(1);
			Account accountEntity = accountRepo.findOneByUsername(cartDTO.getCreatedby());
			cartEntity.setCreatedby(cartDTO.getCreatedby());
			cartEntity.setAccountCart(accountEntity);
			cartEntity.getListProduct().add(productEntity);
			cartEntity.setQuantity(1);
			cartEntity.setCheckout(false);
			long total= 0;
			if(productEntity.getPricePresent() != null) {
				total += productEntity.getPricePresent() ;
			}else {
				total += productEntity.getPriceOld();
			}
			cartEntity.setTotalPrice(total);
		}
		
		repo.save(cartEntity);	
	}

	@Override
	public Cart get(Long id) {
		return repo.findById(id).get();
	}

	@Override
	public void delete(Long id) {
		
		repo.deleteById(id);	
	}
	public void deleteItem(UUID id, String username) {
		Cart cartEntity = new Cart();
		List<Cart> listCart = repo.findAll();
		boolean checkCart = false;
		int checkProduct = -1;
		for (Cart cart : listCart) {	
			//Kiểm tra sản phẩm có nằm trong giỏ hàng của account
			if(cart.getAccountCart().getUsername().equalsIgnoreCase(username)){
				checkCart = true;
				cartEntity = cart;
				//Product isExist
				
				checkProduct = cart.getListProduct().indexOf(productRepo.findById(id).get());
				
			}
		}
		if(checkCart == true) {
			Product productEntity = productRepo.findById(id).get();
			if(checkProduct != -1) {
				cartEntity.setQuantity(cartEntity.getQuantity() - productEntity.getQuantity());
				cartEntity.getListProduct().remove(checkProduct);
			}
			long total= 0;
			for (Product product : cartEntity.getListProduct()) {
				if(product.getPricePresent() != null) {
					total += product.getPricePresent() *  product.getQuantity();
				}else {
					total += product.getPriceOld() *  product.getQuantity();
				}
			}
			cartEntity.setTotalPrice(total);
		}
		repo.save(cartEntity);
	}
	
}
