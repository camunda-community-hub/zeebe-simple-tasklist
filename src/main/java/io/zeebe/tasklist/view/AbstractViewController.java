package io.zeebe.tasklist.view;

import io.zeebe.tasklist.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractViewController {

  private static final int FIRST_PAGE = 0;
  private static final int PAGE_RANGE = 2;

  @Autowired private WhitelabelProperties whitelabelProperties;
  @Autowired private WhitelabelPropertiesMapper whitelabelPropertiesMapper;

  protected void addPaginationToModel(
      final Map<String, Object> model, final Pageable pageable, final long count) {

    final int currentPage = pageable.getPageNumber();
    model.put("currentPage", currentPage);
    model.put("page", currentPage + 1);
    if (currentPage > 0) {
      model.put("prevPage", currentPage - 1);
    }
    if (count > (1 + currentPage) * pageable.getPageSize()) {
      model.put("nextPage", currentPage + 1);
    }
  }

  /*
   * Needs to be added manually, since Spring does not detect @ModelAttribute in abstract classes.
   */
  protected void addDefaultAttributesToModel(Map<String, Object> model) {
    whitelabelPropertiesMapper.addPropertiesToModel(model, whitelabelProperties);
  }

  protected void addCommonsToModel(Map<String, Object> model) {

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final List<String> authorities =
            authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

    final UserDto userDto = new UserDto();
    userDto.setName(authentication.getName());

    final boolean isAdmin = authorities.contains("ROLE_" + Roles.ADMIN);
    userDto.setAdmin(isAdmin);

    model.put("user", userDto);
  }
}
