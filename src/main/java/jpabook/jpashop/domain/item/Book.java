package jpabook.jpashop.domain.item;

import jpabook.jpashop.controller.BookForm;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B")
@Getter
@Setter
@NoArgsConstructor
public class Book extends Item {

    private String author;
    private String isbn;

    @Builder(
            builderClassName = "init"
            , builderMethodName = "initBook"
    )
    public Book(BookForm bookForm, Long itemId) {
        setId(itemId);
        setName(bookForm.getName());
        setPrice(bookForm.getPrice());
        setStockQuantity(bookForm.getStockQuantity());
        setAuthor(bookForm.getAuthor());
        setIsbn(bookForm.getIsbn());
    }
}
