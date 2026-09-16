package br.com.system.services;

import br.com.system.exception.InsufficientStockException;
import br.com.system.model.Product;
import br.com.system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    public void increase(Product product, int quantity) {
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }

    public void decrease(Product product, int quantity) {
        int updatedQuantity = product.getQuantity() - quantity;
        if (updatedQuantity < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName()
            );
        }

        product.setQuantity(updatedQuantity);
        productRepository.save(product);
    }

    public void setQuantity(Product product, int quantity) {
        product.setQuantity(quantity);
        productRepository.save(product);
    }
}
